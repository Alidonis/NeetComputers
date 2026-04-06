package com.redtoast.blocks.OfficeComputer;

import com.redtoast.blocks.Generics.ComputerRenderer;
import com.redtoast.graphics.RotationTools;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class OfficeComputerRenderer extends ComputerRenderer<OfficeEntityComputer> {
    public OfficeComputerRenderer(BlockEntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Vector3f getOffset(OfficeEntityComputer i) {
        return new Vector3f(2.5f / 16f, 5.5f / 16f, 12.5f / 16f);
    }

    @Override
    public Matrix3f getRotation(OfficeEntityComputer i) {
        return RotationTools.generateRotationX(0);
    }
}