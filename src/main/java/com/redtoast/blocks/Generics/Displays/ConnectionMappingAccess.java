package com.redtoast.blocks.Generics.Displays;

import net.minecraft.util.math.BlockPos;
import org.joml.Vector2i;

public interface ConnectionMappingAccess {
    void calculateModel(Vector2i size, Vector2i relativePosition);
    void setSlave(BlockPos masterPos);
    void setMaster(Vector2i size);
}
