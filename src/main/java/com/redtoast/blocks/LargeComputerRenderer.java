package com.redtoast.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class LargeComputerRenderer implements BlockEntityRenderer<LargeEntityComputer> {
    public LargeComputerRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(LargeEntityComputer entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = entity.getCachedState();

        if (!state.get(LargeBlockComputer.ON)) {
            return;
        }

        Direction facing = entity.getCachedState().get(Properties.HORIZONTAL_FACING);

        RenderLayer layer = RenderLayer.getCutout();
        VertexConsumer vc = vertexConsumers.getBuffer(layer);
        Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(new Identifier("neetcomputers", "block/front_on_uv"));

        matrices.push();

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
        matrices.translate(-0.5, -0.5, -0.5);

        Matrix4f mat = matrices.peek().getPositionMatrix();

        float z =  1f;
        float x1 = 2f / 16f, x2 = 2f / 16f + x1;
        float y1 = 1f / 16f, y2 = 1f / 16f + y1;

        vc.vertex(mat, x1, y1, z).color(255, 255, 255, 255).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        vc.vertex(mat, x2, y1, z).color(255, 255, 255, 255).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        vc.vertex(mat, x2, y2, z).color(255, 255, 255, 255).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        vc.vertex(mat, x1, y2, z).color(255, 255, 255, 255).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();

        matrices.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(LargeEntityComputer blockEntity) {
        return BlockEntityRenderer.super.rendersOutsideBoundingBox(blockEntity);
    }
}
