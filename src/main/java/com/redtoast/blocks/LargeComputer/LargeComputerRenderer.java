package com.redtoast.blocks.LargeComputer;

import com.redtoast.blocks.Generics.ComputerRenderer;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class LargeComputerRenderer extends ComputerRenderer<LargeEntityComputer> {

    @Override
    public Vector3f getOffset(LargeEntityComputer i) {
        return new Vector3f(0.125f, 0.1875f, 1);
    }

    @Override
    public Matrix3f getRotation(LargeEntityComputer i) {
        return new Matrix3f(
                1, 0, 0,
                0, 1, 0,
                0, 0, 1
        );
    }
}