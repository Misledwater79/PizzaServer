package io.github.willqi.pizzaserver.server.entity;

import io.github.willqi.pizzaserver.api.entity.Entity;
import io.github.willqi.pizzaserver.api.entity.LivingEntity;
import io.github.willqi.pizzaserver.api.entity.meta.EntityMetaData;
import io.github.willqi.pizzaserver.api.entity.meta.properties.EntityMetaPropertyName;
import io.github.willqi.pizzaserver.api.entity.types.EntityType;
import io.github.willqi.pizzaserver.api.entity.types.behaviour.EntityBehaviour;
import io.github.willqi.pizzaserver.api.level.world.World;
import io.github.willqi.pizzaserver.api.player.Player;
import io.github.willqi.pizzaserver.api.utils.Location;
import io.github.willqi.pizzaserver.commons.utils.NumberUtils;
import io.github.willqi.pizzaserver.commons.utils.Vector3;
import io.github.willqi.pizzaserver.server.ImplServer;
import io.github.willqi.pizzaserver.server.entity.meta.ImplEntityMetaData;
import io.github.willqi.pizzaserver.server.level.ImplLevel;
import io.github.willqi.pizzaserver.server.level.world.ImplWorld;
import io.github.willqi.pizzaserver.server.level.world.chunks.ImplChunk;
import io.github.willqi.pizzaserver.api.network.protocol.packets.RemoveEntityPacket;
import io.github.willqi.pizzaserver.api.network.protocol.packets.SetEntityDataPacket;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseEntity implements Entity {

    public static long ID = 1;

    protected final long id;
    protected volatile float x;
    protected volatile float y;
    protected volatile float z;
    protected volatile World world;
    protected boolean moveUpdate;

    protected float movementSpeed;

    protected float pitch;
    protected float yaw;
    protected float headYaw;

    protected EntityBehaviour behaviour;
    protected final EntityType entityType;

    protected EntityMetaData metaData = new ImplEntityMetaData();

    protected boolean spawned;
    protected final Set<Player> spawnedTo = new HashSet<>();
    protected final Set<Player> hiddenFrom = new HashSet<>();


    public BaseEntity(EntityType entityType) {
        this.id = ID++;
        this.entityType = entityType;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public EntityType getEntityType() {
        return this.entityType;
    }

    @Override
    public float getX() {
        return this.x;
    }

    @Override
    public float getY() {
        return this.y;
    }

    @Override
    public float getZ() {
        return this.z;
    }

    @Override
    public int getFloorX() {
        return (int) Math.floor(this.x);
    }

    @Override
    public int getFloorY() {
        return (int) Math.floor(this.y);
    }

    @Override
    public int getFloorZ() {
        return (int) Math.floor(this.z);
    }

    @Override
    public Location getLocation() {
        return new Location(this.world, new Vector3(this.getX(), this.getY(), this.getZ()));
    }

    /**
     * Set the location of the entity.
     * Used internally to setup and to clean up the entity
     * @param location entity location
     */
    public void setLocation(Location location) {
        if (location != null) {
            this.x = location.getX();
            this.y = location.getY();
            this.z = location.getZ();
            this.world = location.getWorld();
        } else {
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.world = null;
        }
    }

    @Override
    public void teleport(float x, float y, float z) {
        this.teleport(this.getWorld(), x, y, z);
    }

    @Override
    public void teleport(World world, float x, float y, float z) {
        this.moveUpdate = true;

        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    @Override
    public float getEyeHeight() {
        return this.getHeight() / 2 + 0.1f;
    }

    @Override
    public void setDisplayName(String name) {
        this.getMetaData().setStringProperty(EntityMetaPropertyName.NAMETAG, name);
    }

    @Override
    public String getDisplayName() {
        return this.getMetaData().getStringProperty(EntityMetaPropertyName.NAMETAG);
    }

    @Override
    public float getMovementSpeed() {
        return this.movementSpeed;
    }

    @Override
    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = Math.max(0, movementSpeed);
    }

    @Override
    public float getPitch() {
        return this.pitch;
    }

    @Override
    public void setPitch(float pitch) {
        this.moveUpdate = true;
        this.pitch = pitch;
    }

    @Override
    public float getYaw() {
        this.moveUpdate = true;
        return this.yaw;
    }

    @Override
    public void setYaw(float yaw) {
        this.moveUpdate = true;
        this.yaw = yaw;
    }

    @Override
    public float getHeadYaw() {
        return this.headYaw;
    }

    @Override
    public void setHeadYaw(float headYaw) {
        this.moveUpdate = true;
        this.headYaw = headYaw;
    }

    @Override
    public Vector3 getDirectionVector() {
        double cosPitch = Math.cos(Math.toRadians(this.getPitch()));
        double x = Math.sin(Math.toRadians(this.getYaw())) * -cosPitch;
        double y = -Math.sin(Math.toRadians(this.getPitch()));
        double z = Math.cos(Math.toRadians(this.getYaw())) * cosPitch;

        return new Vector3((float) x, (float) y, (float) z).normalize();
    }

    @Override
    public ImplChunk getChunk() {
        return (ImplChunk) this.getLocation().getChunk();
    }

    @Override
    public ImplWorld getWorld() {
        return (ImplWorld) this.getLocation().getWorld();
    }

    @Override
    public ImplLevel getLevel() {
        return (ImplLevel) this.getLocation().getLevel();
    }

    @Override
    public ImplServer getServer() {
        return (ImplServer) ImplServer.getInstance();
    }

    @Override
    public EntityMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public void setMetaData(EntityMetaData metaData) {
        this.metaData = metaData;

        SetEntityDataPacket entityDataPacket = new SetEntityDataPacket();
        entityDataPacket.setRuntimeId(this.getId());
        entityDataPacket.setData(this.getMetaData());
        for (Player player : this.getViewers()) {
            player.sendPacket(entityDataPacket);
        }
    }

    public void moveTo(float x, float y, float z) {
        this.moveUpdate = true;

        ImplChunk currentChunk = this.getChunk();
        this.x = x;
        this.y = y;
        this.z = z;

        ImplChunk newChunk = this.getWorld().getChunk((int) Math.floor(this.x / 16), (int) Math.floor(this.z / 16));
        if (!currentChunk.equals(newChunk)) {   // spawn entity in new chunk and remove from old chunk
            currentChunk.removeEntity(this);
            newChunk.addEntity(this);
        }
    }

    @Override
    public void tick() {
        this.moveUpdate = false;
    }

    @Override
    public EntityBehaviour getEntityBehaviour() {
        return this.behaviour;
    }

    @Override
    public void setEntityBehaviour(EntityBehaviour behaviour) {
        this.behaviour = behaviour;
    }

    /**
     * Called when the entity is initially spawned into a world.
     * This is useful for entity initialization.
     */
    public void onSpawned() {
        this.spawned = true;
    }

    /**
     * Called when the entity is completely despawned.
     */
    public void onDespawned() {
        this.spawned = false;
    }

    public boolean withinEntityRenderDistanceTo(Player player) {
        int chunkDistanceToViewer = (int) Math.round(Math.sqrt(Math.pow(player.getChunk().getX() - this.getChunk().getX(), 2) + Math.pow(player.getChunk().getZ() - this.getChunk().getZ(), 2)));
        return chunkDistanceToViewer < this.getWorld().getServer().getConfig().getEntityChunkRenderDistance();
    }

    public boolean canBeSpawnedTo(Player player) {
        return !this.equals(player)
                && !this.hasSpawnedTo(player)
                && (!(this instanceof LivingEntity) || !((LivingEntity) this).isHiddenFrom(player))
                && this.withinEntityRenderDistanceTo(player)
                && this.getChunk().canBeVisibleTo(player);
    }

    public boolean shouldBeDespawnedFrom(Player player) {
        return (this.getChunk() == null || !this.getChunk().canBeVisibleTo(player) || !this.withinEntityRenderDistanceTo(player))
                && this.hasSpawnedTo(player);
    }

    @Override
    public boolean hasSpawned() {
        return this.spawned;
    }

    @Override
    public boolean hasSpawnedTo(Player player) {
        return this.spawnedTo.contains(player);
    }

    @Override
    public boolean spawnTo(Player player) {
        if (!this.withinEntityRenderDistanceTo(player)) {
            return false;
        }

        if (this.isHiddenFrom(player)) {
            // The entity is being spawned to the player. Unhide the entity.
            this.hiddenFrom.remove(player);
        }

        return this.spawnedTo.add(player);
    }

    @Override
    public boolean despawnFrom(Player player) {
        if (this.spawnedTo.remove(player)) {
            RemoveEntityPacket entityPacket = new RemoveEntityPacket();
            entityPacket.setUniqueEntityId(this.getId());
            player.sendPacket(entityPacket);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void despawn() {
        this.getWorld().removeEntity(this);
    }

    @Override
    public void showTo(Player player) {
        this.hiddenFrom.remove(player);
        if (this.getChunk().canBeVisibleTo(player) && this.withinEntityRenderDistanceTo(player) && !this.hasSpawnedTo(player)) {
            this.spawnTo(player);
        }
    }

    @Override
    public void hideFrom(Player player) {
        this.hiddenFrom.add(player);
        if (this.getViewers().contains(player)) {
            this.despawnFrom(player);
        }
    }

    @Override
    public boolean isHiddenFrom(Player player) {
        return this.hiddenFrom.contains(player);
    }

    @Override
    public Set<Player> getViewers() {
        return new HashSet<>(this.spawnedTo);
    }

    @Override
    public int hashCode() {
        return 43 * (int) this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseEntity) {
            return NumberUtils.isNearlyEqual(((BaseEntity) obj).getId(), this.getId());
        }
        return false;
    }
}
