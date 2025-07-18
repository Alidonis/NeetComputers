package com.redtoast.blocks.LargeComputer;

import com.redtoast.blocks.generic.ComputerBlockEntity;
import com.redtoast.computerSpecs;
import com.redtoast.neet.BulkRegistery;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class LargeEntityComputer extends ComputerBlockEntity {
    public LargeEntityComputer(BlockPos pos, BlockState state) {
        super(BulkRegistery.fetchBlockEntityType("large_computer"), pos, state, new computerSpecs()
            .setBinaryGraphicsSize(12, 11)
            .setColorGraphicsSize(192,108)
            .setIPS(200)
            .setMaxCores(6)
            .setCoreUtilizationBonus(15)
            .setMachineName("Large Computer")
        );
    }
}