package com.redtoast.blocks.Generics.Displays;

import net.minecraft.util.math.Vec3i;

public interface BinaryGraphicsRenderProvider extends BinaryGraphicsProvider {
    Vec3i getColoration(float clock, int x, int y);
    boolean canRender();
}
