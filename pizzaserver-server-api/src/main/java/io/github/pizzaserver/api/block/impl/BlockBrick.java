package io.github.pizzaserver.api.block.impl;

import io.github.pizzaserver.api.block.Block;
import io.github.pizzaserver.api.block.BlockID;
import io.github.pizzaserver.api.item.data.ToolTier;
import io.github.pizzaserver.api.item.data.ToolType;

public class BlockBrick extends Block {

    @Override
    public String getBlockId() {
        return BlockID.BRICK;
    }

    @Override
    public String getName() {
        return "Brick Block";
    }

    @Override
    public float getHardness() {
        return 2;
    }

    @Override
    public float getBlastResistance() {
        return 6;
    }

    @Override
    public ToolType getToolTypeRequired() {
        return ToolType.PICKAXE;
    }

    @Override
    public ToolTier getToolTierRequired() {
        return ToolTier.WOOD;
    }

}
