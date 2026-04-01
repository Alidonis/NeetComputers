package com.redtoast.blocks.LargeComputer;

import com.redtoast.blocks.Generics.ComputerBlock;
import com.redtoast.neet.BulkRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

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
        return BulkRegistry.fetchBlockEntityType("large_computer");
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LargeEntityComputer(pos, state);
    }
}