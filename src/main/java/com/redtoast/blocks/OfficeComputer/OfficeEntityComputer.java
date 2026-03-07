package com.redtoast.blocks.OfficeComputer;

import com.redtoast.blocks.Generics.ComputerBlockEntity;
import com.redtoast.neet.BulkRegistery;
import com.redtoast.simulation.config.DefaultComputerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class OfficeEntityComputer extends ComputerBlockEntity{
    public OfficeEntityComputer(BlockPos pos, BlockState state) {
        super(BulkRegistery.fetchBlockEntityType("office_computer"), pos, state, new DefaultComputerConfig("Office Computer", 11, 6));
    }
}