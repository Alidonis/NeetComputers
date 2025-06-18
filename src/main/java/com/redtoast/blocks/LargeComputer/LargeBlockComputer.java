package com.redtoast.blocks.LargeComputer;

import com.redtoast.blocks.generic.ComputerBlock;
import com.redtoast.neet.BulkRegistery;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class LargeBlockComputer extends ComputerBlock {
    public LargeBlockComputer(Settings settings) {
        super(settings);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntityType<? extends BlockEntity> getType() {
        return BulkRegistery.fetchBlockEntityType("large_computer");
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LargeEntityComputer(pos, state);
    }
}