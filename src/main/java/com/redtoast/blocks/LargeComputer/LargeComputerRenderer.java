package com.redtoast.blocks.LargeComputer;

import com.redtoast.graphics.BinaryGraphicsArray;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix4f;

public class LargeComputerRenderer implements BlockEntityRenderer<LargeEntityComputer> {
    float clock;
    static int opacity = 255;
    public LargeComputerRenderer(BlockEntityRendererFactory.Context context) {
        clock = 0;
    }

    @Override
    public void render(LargeEntityComputer entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isPaused() && client.isInSingleplayer()){
            clock += tickDelta * 0.01f;
        }
        clock %= 6.2f;
        BlockState state = entity.getCachedState();

        if (!state.get(LargeBlockComputer.ON)) {
            return;
        }
        BlockPos pos = entity.getPos();
        Direction facing = entity.getCachedState().get(Properties.HORIZONTAL_FACING);

        if (facing.equals(Direction.NORTH)){
            pos = pos.north();
        } else if (facing.equals(Direction.EAST)) {
            pos = pos.east();
        } else if (facing.equals(Direction.SOUTH)) {
            pos = pos.south();
        } else {
            pos = pos.west();
        }
        if (entity.getWorld()!=null){
            BlockState state2 = entity.getWorld().getBlockState(pos);
            if (state2.isOpaque() && state2.isFullCube(entity.getWorld(),pos)) return;
        }

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

        vc.vertex(mat, x1, y1, z).color(255, 255, 255, opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        vc.vertex(mat, x2, y1, z).color(255, 255, 255, opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        vc.vertex(mat, x2, y2, z).color(255, 255, 255, opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        vc.vertex(mat, x1, y2, z).color(255, 255, 255, opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();

        if (entity.computer.getBinaryGraphics()!=null){
            sprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(new Identifier("neetcomputers", "block/white"));
            BinaryGraphicsArray graphics = entity.computer.getBinaryGraphics();
            int sizex = graphics.getSize().x, sizey = graphics.getSize().y;
            for (int x = 0; x < sizex; x++){
                for (int y = 0; y < sizey; y++){
                    if (graphics.get(x,y)){
                        drawPixel(x,y,vc,mat,overlay,sprite, entity);
                    }
                }
            }
        }

        matrices.pop();
    }

    private void drawPixel(int x, int y, VertexConsumer vc, Matrix4f mat, int overlay, Sprite sprite, LargeEntityComputer entity){
        float x1 = x / 16f, x2 = 1f / 16f + x1;
        float y1 = y / 16f, y2 = 1f / 16f + y1;
        float z1 = 15.5f / 16f, z2 = 15f / 16f;
        float offsetX = 2f / 16f, offsetY = 3f / 16f;
        Vec3i rgb = getPixel(x, y);
        BinaryGraphicsArray graphics = entity.computer.getBinaryGraphics();
        vc.vertex(mat, x1 + offsetX, y1 + offsetY, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        vc.vertex(mat, x2 + offsetX, y1 + offsetY, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        vc.vertex(mat, x2 + offsetX, y2 + offsetY, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        vc.vertex(mat, x1 + offsetX, y2 + offsetY, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        x1 += offsetX;
        x2 += offsetX;
        y1 += offsetY;
        y2 += offsetY;
        if (!graphics.get(x, y+1)){
            vc.vertex(mat, x1, y2, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
            vc.vertex(mat, x2, y2, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
            vc.vertex(mat, x2, y2, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
            vc.vertex(mat, x1, y2, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        }
        if (!graphics.get(x, y-1)){
            vc.vertex(mat, x2, y1, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
            vc.vertex(mat, x1, y1, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
            vc.vertex(mat, x1, y1, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
            vc.vertex(mat, x2, y1, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        }
        if (!graphics.get(x+1, y)){
            vc.vertex(mat, x2, y2, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
            vc.vertex(mat, x2, y1, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
            vc.vertex(mat, x2, y1, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
            vc.vertex(mat, x2, y2, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        }
        if (!graphics.get(x-1, y)){
            vc.vertex(mat, x1, y1, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
            vc.vertex(mat, x1, y2, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
            vc.vertex(mat, x1, y2, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
            vc.vertex(mat, x1, y1, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
        }
    }

    private Vec3i getPixel(int x, int y){
        int r = 40;
        int g = 226;
        int b = 50;
        if ((x+2)%3==0){
            r++;
            g += 8;
            b += 3;
        }
        if ((y+1)%2==0){
            r++;
            g += 10;
            b += 3;
        }
        float density = 0.4f;
        float offset = (clock + y * density) % 6.2f;
        double weight = 8;
        double effect = Math.sin(offset) * weight;
        r += (int)Math.round(effect);
        g += (int)Math.round(effect);
        b += (int)Math.round(effect);
        return new Vec3i(r,g,b);
    }

    @Override
    public boolean rendersOutsideBoundingBox(LargeEntityComputer blockEntity) {
        return BlockEntityRenderer.super.rendersOutsideBoundingBox(blockEntity);
    }
}
