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
            //Class.forName("dan200.computercraft.api.peripheral.IPeripheral;");
            return CCT.searchForCCT(pos, world);
        }// catch (ClassNotFoundException e) {
            // LOGGER = LoggerFactory.getLogger("NeetComputers");
           // LOGGER.warn("Computer Craft not installed");
            //return null;
        //}
        catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
