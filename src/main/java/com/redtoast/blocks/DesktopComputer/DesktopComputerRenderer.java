package com.redtoast.blocks.DesktopComputer;

import com.redtoast.blocks.Generics.ComputerRenderer;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class DesktopComputerRenderer extends ComputerRenderer<DesktopEntityComputer> {

    @Override
    public Vector3f getOffset(DesktopEntityComputer i) {
        return new Vector3f(2.5f / 16f, 5.0206f / 16f, 1 - 3.1481f / 16f);
    }

    @Override
    public Matrix3f getRotation(DesktopEntityComputer i) {
        return new Matrix3f(
                1, 0, 0,
                0, 0.92387952551f, -0.38268344927f,
                0, 0.38268344927f, 0.92387952551f
        );
    }
}
