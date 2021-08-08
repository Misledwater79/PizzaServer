package io.github.willqi.pizzaserver.api.entity;

import io.github.willqi.pizzaserver.api.entity.meta.EntityMetaData;
import io.github.willqi.pizzaserver.api.player.Player;
import io.github.willqi.pizzaserver.api.utils.Location;
import io.github.willqi.pizzaserver.api.utils.Watchable;
import io.github.willqi.pizzaserver.api.world.World;
import io.github.willqi.pizzaserver.api.world.chunks.Chunk;

/**
 * Represents a entity on Minecraft
 */
public interface Entity extends Watchable {

    long getId();

    float getX();

    float getY();

    float getZ();

    int getFloorX();

    int getFloorY();

    int getFloorZ();

    /**
     * Retrieve the {@link World} the entity is in
     * @return the {@link World}
     */
    World getWorld();

    /**
     * Retrieve the {@link Chunk} the entity is in
     * @return the {@link Chunk}
     */
    Chunk getChunk();

    /**
     * Retrieve the {@link Location} of the entity
     * @return the {@link Location}
     */
    Location getLocation();

    EntityMetaData getMetaData();

    void setMetaData(EntityMetaData metaData);

    /**
     * Check if the entity has been spawned into a world yet
     * @return if the entity has been spawned into a world
     */
    boolean hasSpawned();

    boolean hasSpawnedTo(Player player);

    void spawnTo(Player player);

    void despawnFrom(Player player);

    void despawn();

}
