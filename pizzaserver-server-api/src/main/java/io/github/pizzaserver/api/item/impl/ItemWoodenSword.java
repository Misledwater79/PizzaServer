package io.github.pizzaserver.api.item.impl;

import io.github.pizzaserver.api.item.BaseItem;
import io.github.pizzaserver.api.item.data.ToolTier;
import io.github.pizzaserver.api.item.data.ToolType;
import io.github.pizzaserver.api.item.descriptors.ToolItem;
import io.github.pizzaserver.api.item.data.ItemID;
import io.github.pizzaserver.api.item.descriptors.DurableItem;

public class ItemWoodenSword extends BaseItem implements DurableItem, ToolItem {

    public ItemWoodenSword() {
        this(1);
    }

    public ItemWoodenSword(int count) {
        this(count, 0);
    }

    public ItemWoodenSword(int count, int meta) {
        super(ItemID.WOODEN_SWORD, count, meta);
    }

    @Override
    public String getName() {
        return "Wooden Sword";
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getDamage() {
        return 5;
    }

    @Override
    public ToolType getToolType() {
        return ToolType.SWORD;
    }

    @Override
    public ToolTier getToolTier() {
        return ToolTier.WOOD;
    }

    @Override
    public int getMaxDurability() {
        return 60;
    }

}
