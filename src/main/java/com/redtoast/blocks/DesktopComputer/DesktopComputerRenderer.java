package com.redtoast.blocks.DesktopComputer;

import com.redtoast.blocks.generic.ComputerRenderer;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.graphics.RotationTools;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class DesktopComputerRenderer extends ComputerRenderer<DesktopEntityComputer> {

    @Override
    public Vector3f getOffset() {
        return new Vector3f(2.5f / 16f, 5.0206f / 16f, 1 - 3.1481f / 16f);
    }

    @Override
    public Matrix3f getRotation() {
        return RotationTools.generateRotationX(22.5);
    }
}
