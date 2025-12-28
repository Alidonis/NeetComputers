package com.redtoast.blocks.LargeComputer;

import com.redtoast.blocks.generic.ComputerRenderer;
import com.redtoast.graphics.RotationTools;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class LargeComputerRenderer extends ComputerRenderer<LargeEntityComputer> {

    @Override
    public Vector3f getOffset() {
        return new Vector3f(0.125f, 0.1875f, 1);
    }

    @Override
    public Matrix3f getRotation() {
        return RotationTools.generateRotationX(0);
    }
}