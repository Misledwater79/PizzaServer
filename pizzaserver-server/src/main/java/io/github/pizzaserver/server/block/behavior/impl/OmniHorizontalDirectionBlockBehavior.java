package io.github.pizzaserver.server.block.behavior.impl;

import com.nukkitx.math.vector.Vector3f;
import io.github.pizzaserver.api.block.Block;
import io.github.pizzaserver.api.block.behavior.impl.DefaultBlockBehavior;
import io.github.pizzaserver.api.block.data.BlockFace;
import io.github.pizzaserver.api.entity.Entity;

public class OmniHorizontalDirectionBlockBehavior<T extends Block> extends DefaultBlockBehavior<T> {

    @Override
    public boolean prepareForPlacement(Entity entity, T block, BlockFace face, Vector3f clickPosition) {
        block.setBlockState(entity.getHorizontalDirection().opposite().getOmniBlockStateIndex());
        return super.prepareForPlacement(entity, block, face, clickPosition);
    }

}
