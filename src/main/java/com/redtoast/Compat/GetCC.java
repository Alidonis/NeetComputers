package com.redtoast.Compat;

import com.redtoast.Computer;
import com.redtoast.Connections.PeripheralProvider;
import com.redtoast.simulation.APILoader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GetCC {
    public static PeripheralProvider getPeripheral(BlockPos pos, World world, Computer computer){
        try {
            return computer == null ? CCT.searchForCCT(pos, world) : CCT.searchForCCT(pos, world, computer);
        } catch (Throwable t) {
            if (!(t instanceof NoClassDefFoundError)) APILoader.printJavaError(t);
            return null;
        }
    }
}
