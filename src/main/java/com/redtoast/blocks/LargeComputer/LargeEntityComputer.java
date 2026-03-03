package com.redtoast.blocks.LargeComputer;

import com.redtoast.blocks.generic.ComputerBlockEntity;
import com.redtoast.neet.BulkRegistery;
import com.redtoast.simulation.config.DefaultComputerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class LargeEntityComputer extends ComputerBlockEntity {
    public LargeEntityComputer(BlockPos pos, BlockState state) {
        super(BulkRegistery.fetchBlockEntityType("large_computer"), pos, state, new DefaultComputerConfig("Large Computer", 12, 11));
    }
}