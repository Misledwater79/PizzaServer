package io.github.willqi.pizzaserver.server.entity;

import io.github.willqi.pizzaserver.api.entity.inventory.LivingEntityInventory;
import io.github.willqi.pizzaserver.api.entity.LivingEntity;
import io.github.willqi.pizzaserver.api.entity.types.EntityType;
import io.github.willqi.pizzaserver.api.level.world.World;
import io.github.willqi.pizzaserver.api.player.Player;
import io.github.willqi.pizzaserver.server.level.world.chunks.ImplChunk;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseLivingEntity extends BaseEntity implements LivingEntity {

    protected float health;
    protected float maxHealth;

    protected float absorption;
    protected float maxAbsorption;

    protected LivingEntityInventory inventory = null;


    public BaseLivingEntity(EntityType entityType) {
        super(entityType);
    }

    @Override
    public float getHealth() {
        return this.health;
    }

    @Override
    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(health, this.getMaxHealth()));

        if (this.getHealth() <= 0) {
            // TODO: kill
        }
    }

    @Override
    public float getMaxHealth() {
        return this.maxHealth;
    }

    @Override
    public void setMaxHealth(float maxHealth) {
        this.maxHealth = Math.max(0, maxHealth);
        this.setHealth(Math.min(this.getHealth(), this.getMaxHealth()));
    }

    @Override
    public float getAbsorption() {
        return this.absorption;
    }

    @Override
    public void setAbsorption(float absorption) {
        this.absorption = Math.max(0, Math.min(absorption, this.getMaxAbsorption()));
    }

    @Override
    public float getMaxAbsorption() {
        return this.maxAbsorption;
    }

    @Override
    public void setMaxAbsorption(float maxAbsorption) {
        this.maxAbsorption = Math.max(0, maxAbsorption);
        this.setAbsorption(Math.min(this.getAbsorption(), this.getMaxAbsorption()));
    }

    @Override
    public LivingEntityInventory getInventory() {
        return this.inventory;
    }

}
