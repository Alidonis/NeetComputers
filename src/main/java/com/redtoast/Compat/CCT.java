package com.redtoast.Compat;

import com.redtoast.Computer;
import com.redtoast.Connections.PeripheralProvider;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CCT {

    public static PeripheralProvider searchForCCT(BlockPos pos, World world, Computer computer){
        IPeripheral peripheral = PeripheralLookup.get().find(world, pos, null);
        if (peripheral==null) return null;
        return new WrappedPeripheral(peripheral, pos, computer);
    }
}
