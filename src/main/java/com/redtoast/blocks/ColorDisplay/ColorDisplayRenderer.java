package com.redtoast.blocks.ColorDisplay;

import com.redtoast.blocks.Generics.Displays.PipeSourceBlockRenderer;
import com.redtoast.graphics.SectoredGraphics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ColorDisplayRenderer extends PipeSourceBlockRenderer<ColorDisplayBlockEntity> {
    private final Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(Identifier.of("neetcomputers", "block/white"));

    @Override
    public void render(ColorDisplayBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        super.render(entity, tickDelta, matrices, vertexConsumers, light, overlay);
        Direction facing = entity.getCachedState().get(Properties.HORIZONTAL_FACING);
        if (!entity.isLeader()) return;

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
        matrices.translate(-0.5, -0.5, -0.5);
        matrices.scale(1f / 16, 1f / 16, 1f / 16);
        matrices.translate(2,18 - entity.getWorld().getBlockState(entity.getPos()).get(ColorDisplayBlock.SCALE) * 16,15);
        matrices.scale(1f / ColorDisplayBlockEntity.pixelDensity, 1f / ColorDisplayBlockEntity.pixelDensity, 1);

        RenderLayer layer = RenderLayer.getCutout();
        VertexConsumer vc = vertexConsumers.getBuffer(layer);
        Matrix4f mat = matrices.peek().getPositionMatrix();

        SectoredGraphics graphics = entity.getRenderGraphics();
        //new Random().nextInt(0x1FFFFFF)|0xFF000000);
        if (graphics==null) {
            matrices.pop();
            return;
        }

        for (SectoredGraphics.Sector sector : graphics) {
            drawPixel(sector.x1(), sector.y1(), sector.x2()+1, sector.y2()+1, vc, mat, overlay, sector.color());
        }

        matrices.pop();
    }

    public void drawPixel(float x1, float y1, float x2, float y2, VertexConsumer vc, Matrix4f mat, int overlay, int color){
        wrapVector(vc.vertex(mat, x1, y1, 0), color, overlay).texture(sprite.getMinU(), sprite.getMinV());
        wrapVector(vc.vertex(mat, x2, y1, 0), color, overlay).texture(sprite.getMaxU(), sprite.getMinV());
        wrapVector(vc.vertex(mat, x2, y2, 0), color, overlay).texture(sprite.getMaxU(), sprite.getMaxV());
        wrapVector(vc.vertex(mat, x1, y2, 0), color, overlay).texture(sprite.getMinU(), sprite.getMaxV());
    }

    public VertexConsumer wrapVector(VertexConsumer consumer, int color, int overlay){
        return consumer.color(color).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
    }

    @Override
    public boolean rendersOutsideBoundingBox(ColorDisplayBlockEntity blockEntity) {
        return super.rendersOutsideBoundingBox(blockEntity);
    }
}
