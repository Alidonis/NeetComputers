package com.redtoast.blocks.ColorDisplay;

import com.redtoast.blocks.Generics.Displays.PipeSourceBlockRenderer;
import com.redtoast.graphics.SectoredGraphics;
import com.redtoast.graphics.client.DisplayTextureManager;
import com.redtoast.graphics.client.ScreenRenderLayers;
import com.redtoast.graphics.client.ScreenTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ColorDisplayRenderer extends PipeSourceBlockRenderer<ColorDisplayBlockEntity> {

    @Override
    public void render(ColorDisplayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        super.render(entity, tickDelta, matrices, vertexConsumers, light, overlay);
        Direction facing = entity.getCachedState().get(Properties.HORIZONTAL_FACING);
        if (!entity.isLeader()) return;

        SectoredGraphics graphics = entity.getRenderGraphics();
        if (graphics == null) return;

        ScreenTexture texture = DisplayTextureManager.get(entity.getPos(), graphics);
        if (texture == null) {
            DisplayTextureManager.remove(entity.getPos());
            return;
        }

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
        matrices.translate(-0.5, -0.5, -0.5);
        matrices.scale(1f / 16, 1f / 16, 1f / 16);
        matrices.translate(2, 2, 15);
        matrices.scale(1f / ColorDisplayBlockEntity.pixelDensity, 1f / ColorDisplayBlockEntity.pixelDensity, 1);

        float w = graphics.size().x;
        float h = graphics.size().y;

        RenderLayer layer = ScreenRenderLayers.getDisplay(texture.getId());
        VertexConsumer vc = vertexConsumers.getBuffer(layer);
        Matrix4f mat = matrices.peek().getPositionMatrix();

        vc.vertex(mat, 0, 0, 0).texture(0, 0).color(255, 255, 255, 255);
        vc.vertex(mat, w, 0, 0).texture(1, 0).color(255, 255, 255, 255);
        vc.vertex(mat, w, h, 0).texture(1, 1).color(255, 255, 255, 255);
        vc.vertex(mat, 0, h, 0).texture(0, 1).color(255, 255, 255, 255);

        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(ColorDisplayBlockEntity blockEntity) {
        return super.rendersOutsideBoundingBox(blockEntity);
    }
}
