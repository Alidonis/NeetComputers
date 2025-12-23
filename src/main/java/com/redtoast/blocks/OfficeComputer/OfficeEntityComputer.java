package com.redtoast.blocks.OfficeComputer;

import com.redtoast.blocks.generic.ComputerBlockEntity;
import com.redtoast.computerSpecs;
import com.redtoast.neet.BulkRegistery;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class OfficeEntityComputer extends ComputerBlockEntity{
    public OfficeEntityComputer(BlockPos pos, BlockState state) {
        super(BulkRegistery.fetchBlockEntityType("office_computer"), pos, state, new computerSpecs()
            .setBinaryGraphicsSize(11, 6)
            .setColorGraphicsSize(384,288)
            .setIPS(500000)
            .setMachineName("Office Computer")
        );
    }
}