package com.redtoast.Connections;

import com.redtoast.Compat.GetCC;
import com.redtoast.Computer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class Connections {
    public static boolean COMPATIBILITY = false;

    public static @Nullable PeripheralProvider getPeripheral(BlockPos pos, World world, @Nullable Computer computer) {
        if (world.getBlockState(pos).getBlock().getClass().isAnnotationPresent(PeripheralBlock.class)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof PeripheralProvider provider) {
                return provider;
            }
        }else if (COMPATIBILITY){
            return GetCC.getPeripheral(pos, world, computer);
        }
        return null;
    }
}
