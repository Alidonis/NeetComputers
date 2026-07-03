package com.redtoast.blocks.OfficeComputer;

import com.redtoast.blocks.Generics.ComputerRenderer;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class OfficeComputerRenderer extends ComputerRenderer<OfficeEntityComputer> {
    @Override
    public Vector3f getOffset(OfficeEntityComputer i) {
        return new Vector3f(2.5f / 16f, 5.5f / 16f, 12.5f / 16f);
    }

    @Override
    public Matrix3f getRotation(OfficeEntityComputer i) {
        return new Matrix3f(
                1, 0, 0,
                0, 1, 0,
                0, 0, 1
        );
    }
}