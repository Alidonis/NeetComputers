package com.redtoast.blocks.DesktopComputer;

import com.redtoast.blocks.generic.ComputerBlockEntity;
import com.redtoast.computerSpecs;
import com.redtoast.neet.BulkRegistery;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class DesktopEntityComputer extends ComputerBlockEntity{
    public DesktopEntityComputer(BlockPos pos, BlockState state) {
        super(BulkRegistery.fetchBlockEntityType("desktop_computer"), pos, state, new computerSpecs()
            .setBinaryGraphicsSize(11, 6)
            .setColorGraphicsSize(192,108)
            .setIPS(500)
            .setMaxCores(3)
            .setCoreUtilizationBonus(0)
            .setMachineName("Desktop Computer")
        );
    }
}