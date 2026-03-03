package com.redtoast.blocks.DesktopComputer;

import com.redtoast.blocks.generic.ComputerBlockEntity;
import com.redtoast.neet.BulkRegistery;
import com.redtoast.simulation.config.DefaultComputerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class DesktopEntityComputer extends ComputerBlockEntity{
    public DesktopEntityComputer(BlockPos pos, BlockState state) {
        super(BulkRegistery.fetchBlockEntityType("desktop_computer"), pos, state, new DefaultComputerConfig("Desktop Computer", 11, 6));
    }
}