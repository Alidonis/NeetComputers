package com.redtoast.Compat;

import com.redtoast.Computer;
import com.redtoast.Connections.PeripheralProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetCC {
    public static PeripheralProvider getPeripheral(BlockPos pos, World world, Computer computer){
        try {
            return computer == null ? CCT.searchForCCT(pos, world) : CCT.searchForCCT(pos, world, computer);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
