package io.github.willqi.pizzaserver.server.network.handlers;

import io.github.willqi.pizzaserver.server.Server;
import io.github.willqi.pizzaserver.server.data.Difficulty;
import io.github.willqi.pizzaserver.server.data.ServerOrigin;
import io.github.willqi.pizzaserver.server.events.player.PreLoginEvent;
import io.github.willqi.pizzaserver.server.network.BedrockClientSession;
import io.github.willqi.pizzaserver.server.network.BedrockPacketHandler;
import io.github.willqi.pizzaserver.server.network.protocol.ServerProtocol;
import io.github.willqi.pizzaserver.server.network.protocol.data.PackInfo;
import io.github.willqi.pizzaserver.server.network.protocol.data.PlayerMovementType;
import io.github.willqi.pizzaserver.server.network.protocol.packets.*;
import io.github.willqi.pizzaserver.server.packs.DataPack;
import io.github.willqi.pizzaserver.server.player.Player;
import io.github.willqi.pizzaserver.server.player.data.Gamemode;
import io.github.willqi.pizzaserver.server.player.data.PermissionLevel;
import io.github.willqi.pizzaserver.server.utils.BlockCoordinates;
import io.github.willqi.pizzaserver.server.utils.Vector2;
import io.github.willqi.pizzaserver.server.utils.Vector3;
import io.github.willqi.pizzaserver.server.world.data.Dimension;
import io.github.willqi.pizzaserver.server.world.data.WorldType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Handles preparing/authenticating a client to becoming a valid player
 * Includes Login > Packs > Starting Packets > PlayStatus - Player Spawn
 */
public class PlayerInitializationPacketHandler extends BedrockPacketHandler {

    private final Server server;
    private final BedrockClientSession session;
    private Player player;


    public PlayerInitializationPacketHandler(Server server, BedrockClientSession session) {
        this.server = server;
        this.session = session;
    }

    @Override
    public void onPacket(LoginPacket packet) {

        if (this.player != null) {
            this.server.getLogger().info("Client tried to login again.");
            this.session.disconnect();
            return;
        }

        if (!ServerProtocol.PACKET_REGISTRIES.containsKey(packet.getProtocol())) {
            PlayStatusPacket loginFailPacket = new PlayStatusPacket();
            if (packet.getProtocol() > ServerProtocol.LATEST_PROTOCOL_VERISON) {
                loginFailPacket.setStatus(PlayStatusPacket.PlayStatus.OUTDATED_SERVER);
            } else {
                loginFailPacket.setStatus(PlayStatusPacket.PlayStatus.OUTDATED_CLIENT);
            }
            this.session.sendPacket(loginFailPacket);
            return;
        }

        if (!packet.isAuthenticated()) {
            this.session.disconnect();
            return;
        }

        Player player = new Player(this.server, this.session, packet);
        this.player = player;

        PreLoginEvent event = new PreLoginEvent(player);
        this.server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            this.session.disconnect();
            return;
        }

        if (this.server.getPlayerCount() >= this.server.getMaximumPlayerCount()) {
            PlayStatusPacket playStatusPacket = new PlayStatusPacket();
            playStatusPacket.setStatus(PlayStatusPacket.PlayStatus.SERVER_FULL);
            player.sendPacket(playStatusPacket);
            return;
        }

        PlayStatusPacket playStatusPacket = new PlayStatusPacket();
        playStatusPacket.setStatus(PlayStatusPacket.PlayStatus.LOGIN_SUCCESS);
        player.sendPacket(playStatusPacket);

        ResourcePacksInfoPacket resourcePacksInfoPacket = new ResourcePacksInfoPacket();
        resourcePacksInfoPacket.setForcedToAccept(this.server.getResourcePackManager().arePacksRequired());
        resourcePacksInfoPacket.setResourcePacks(
                this.server.getResourcePackManager()
                        .getResourcePacks()
                        .values().toArray(new DataPack[0])
        );
        resourcePacksInfoPacket.setBehaviorPacks(
                this.server.getResourcePackManager()
                    .getBehaviorPacks()
                    .values().toArray(new DataPack[0])
        );
        player.sendPacket(resourcePacksInfoPacket);
    }

    @Override
    public void onPacket(ViolationPacket packet) {
        throw new AssertionError("ViolationPacket for packet id " + packet.getPacketId() + " " + packet.getMessage());
    }


    @Override
    public void onPacket(ResourcePackResponsePacket packet) {

        if (this.player == null) {
            this.server.getLogger().error("Client requested packs before player object was created.");
            this.session.disconnect();
            return;
        }

        switch (packet.getStatus()) {
            case HAVE_ALL_PACKS:
                // Send required starting packets to client now that they have all resource packs.
                this.sendGameLoginPackets();
                break;
            case SEND_PACKS:
                // Send all pack info of the packs the client does not have
                for (PackInfo packInfo : packet.getPacksRequested()) {

                    if (this.server.getResourcePackManager().getResourcePacks().containsKey(packInfo.getUuid())) {
                        DataPack pack = this.server.getResourcePackManager().getResourcePacks().get(packInfo.getUuid());
                        ResourcePackDataInfoPacket resourcePackDataInfoPacket = new ResourcePackDataInfoPacket();
                        resourcePackDataInfoPacket.setPackId(pack.getUuid());
                        resourcePackDataInfoPacket.setHash(pack.getHash());
                        resourcePackDataInfoPacket.setVersion(pack.getVersion());
                        resourcePackDataInfoPacket.setType(ResourcePackDataInfoPacket.PackType.RESOURCE_PACK);
                        resourcePackDataInfoPacket.setChunkCount(pack.getChunkCount());
                        resourcePackDataInfoPacket.setCompressedPackageSize(pack.getDataLength());
                        resourcePackDataInfoPacket.setMaxChunkSize(DataPack.CHUNK_LENGTH);
                        this.player.sendPacket(resourcePackDataInfoPacket);
                    } else {
                        this.server.getLogger().error("Client requested invalid pack.");
                        this.session.disconnect();
                        break;
                    }

                }
                break;
            case REFUSED:
                if (this.server.getResourcePackManager().arePacksRequired()) {
                    this.session.disconnect();
                } else {
//                    this.sendGameLoginPackets();
                }
                break;
        }
    }

    @Override
    public void onPacket(ResourcePackChunkRequestPacket packet) {
        if (this.player == null) {
            this.server.getLogger().error("Client requested resource pack chunk before player object was created.");
            this.session.disconnect();
            return;
        }

        if (!this.server.getResourcePackManager().getResourcePacks().containsKey(packet.getPackInfo().getUuid()) && !this.server.getResourcePackManager().getBehaviorPacks().containsKey(packet.getPackInfo().getUuid())) {
            this.server.getLogger().error("Invalid resource pack UUID specified while handling ResourcePackChunkRequestPacket.");
            this.session.disconnect();
            return;
        }

        DataPack pack = this.server.getResourcePackManager().getResourcePacks().getOrDefault(
                packet.getPackInfo().getUuid(),
                this.server.getResourcePackManager().getBehaviorPacks().get(packet.getPackInfo().getUuid()));

        if (packet.getChunkIndex() < 0 || packet.getChunkIndex() >= pack.getChunkCount()) {
            this.server.getLogger().error("Invalid chunk requested while handling ResourcePackChunkRequestPacket");
            this.session.disconnect();
            return;
        }
        
        ResourcePackChunkDataPacket chunkDataPacket = new ResourcePackChunkDataPacket();
        chunkDataPacket.setId(pack.getUuid());
        chunkDataPacket.setVersion(pack.getVersion());
        chunkDataPacket.setChunkIndex(packet.getChunkIndex());
        chunkDataPacket.setChunkProgress((long)packet.getChunkIndex() * DataPack.CHUNK_LENGTH);  // Where to continue the download process from
        chunkDataPacket.setData(pack.getChunk(packet.getChunkIndex()));
        this.player.sendPacket(chunkDataPacket);
    }

    /**
     * Called when the player has passed the resource packs stage and is ready to start the game login process.
     */
    private void sendGameLoginPackets() {

        StartGamePacket startGamePacket = new StartGamePacket();

        // Entity specific
        startGamePacket.setDimension(Dimension.OVERWORLD);
        startGamePacket.setEntityId(this.player.getId());
        startGamePacket.setPlayerGamemode(Gamemode.SURVIVAL);
        startGamePacket.setPlayerPermissionLevel(PermissionLevel.MEMBER);
        startGamePacket.setRuntimeEntityId(this.player.getId());
        startGamePacket.setPlayerRotation(new Vector2(0, 0));
        startGamePacket.setPlayerSpawn(new Vector3(0, 0, 0));

        // Server
        startGamePacket.setChunkTickRange(4);    // TODO: modify once you get chunks ticking
        startGamePacket.setCommandsEnabled(true);
        // packet.setCurrentTick(0);       // TODO: get actual tick count
        startGamePacket.setDefaultGamemode(Gamemode.SURVIVAL);
        startGamePacket.setDifficulty(Difficulty.PEACEFUL);
        // packet.setEnchantmentSeed(0);   // TODO: find actual seed
        startGamePacket.setGameVersion(ServerProtocol.GAME_VERSION);
        startGamePacket.setServerName("Testing");
        startGamePacket.setMovementType(PlayerMovementType.CLIENT_AUTHORITATIVE);
        startGamePacket.setServerAuthoritativeBlockBreaking(true);
        startGamePacket.setServerAuthoritativeInventory(true);
        startGamePacket.setResourcePacksRequired(this.server.getResourcePackManager().arePacksRequired());
        startGamePacket.setServerOrigin(ServerOrigin.NONE);

        // World
        startGamePacket.setWorldSpawn(new BlockCoordinates(0, 0, 0));
        startGamePacket.setWorldId(Base64.getEncoder().encodeToString(startGamePacket.getServerName().getBytes(StandardCharsets.UTF_8)));
        startGamePacket.setWorldType(WorldType.INFINITE);


        CreativeContentPacket creativeContentPacket = new CreativeContentPacket();

        this.player.sendPacket(startGamePacket);
        this.player.sendPacket(creativeContentPacket);

    }

//
//    /**
//     * This requires the player to be authenticated before calling the method
//     * @return CreativeContentPacket for the player's target protocol version
//     */
//    private CreativeContentPacket getCreativeContentPacket() {
//        CreativeContentPacket packet = new CreativeContentPacket();
//        packet.setContents(ItemPalette.getCreativeContents(this.player.getLoginData().getProtocolVersion()));
//        return packet;
//    }
//
//    /**
//     * This requires the player to be authenticated before calling the method
//     * @return BiomeDefinitionListPacket for the player's target protocol version
//     */
//    private BiomeDefinitionListPacket getBiomesPacket() {
//        BiomeDefinitionListPacket packet = new BiomeDefinitionListPacket();
//        packet.setDefinitions(WorldPalette.getBiomesPaletteNbt(this.player.getLoginData().getProtocolVersion()));
//        return packet;
//    }
//
}
