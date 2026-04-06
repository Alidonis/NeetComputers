package com.redtoast.blocks.LargeComputer;

import com.redtoast.blocks.Generics.ComputerBlockEntity;
import com.redtoast.neet.BulkRegistry;
import com.redtoast.neet.NeetComputersServer;
import com.redtoast.simulation.config.DefaultComputerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class LargeEntityComputer extends ComputerBlockEntity {
    public LargeEntityComputer(BlockPos pos, BlockState state) {
        super(NeetComputersServer.largeComputerType, pos, state, new DefaultComputerConfig("Large Computer", 12, 11));
    }
}