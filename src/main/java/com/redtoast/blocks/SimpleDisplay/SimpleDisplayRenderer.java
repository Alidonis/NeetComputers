package com.redtoast.blocks.SimpleDisplay;

import com.redtoast.blocks.Generics.ComputerRenderer;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class SimpleDisplayRenderer extends ComputerRenderer<SimpleDisplayBlockEntity> {
    @Override
    public Vector3f getOffset(SimpleDisplayBlockEntity blockEntity) {
        return new Vector3f(0.125f, 0.125f, 1);
    }

    @Override
    public Matrix3f getRotation(SimpleDisplayBlockEntity blockEntity) {
        return new Matrix3f(
                1, 0, 0,
                0, 1, 0,
                0, 0, 1
        );
    }
}
