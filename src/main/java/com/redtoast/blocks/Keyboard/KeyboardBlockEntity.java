package com.redtoast.blocks.Keyboard;

import com.redtoast.blocks.Generics.PeripheralBlockEntity;
import com.redtoast.neet.BulkRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class KeyboardBlockEntity extends PeripheralBlockEntity {
    public KeyboardBlockEntity(BlockPos pos, BlockState state) {
        super(BulkRegistry.fetchBlockEntityType("keyboard"), pos, state, "neetcomputers:keyboard");
    }
}
