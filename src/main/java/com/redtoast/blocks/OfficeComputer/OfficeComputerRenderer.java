package com.redtoast.blocks.OfficeComputer;

import com.redtoast.blocks.Generics.ComputerRenderer;
import com.redtoast.graphics.RotationTools;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class OfficeComputerRenderer extends ComputerRenderer<OfficeEntityComputer> {
    @Override
    public Vector3f getOffset() {
        return new Vector3f(2.5f / 16f, 5.5f / 16f, 12.5f / 16f);
    }

    @Override
    public Matrix3f getRotation() {
        return RotationTools.generateRotationX(0);
    }
}