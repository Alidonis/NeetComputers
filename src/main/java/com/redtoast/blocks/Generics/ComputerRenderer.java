package com.redtoast.blocks.Generics;

import com.redtoast.graphics.BinaryGraphicsArray;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class  ComputerRenderer<BlockEntityType extends BlockEntity> extends PipeSourceBlockRenderer<BlockEntityType> {
    float clock = 0;
    static int opacity = 255;

    @Override
    public void render(BlockEntityType entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        clock %= 6.2f;

        super.render(entity, tickDelta, matrices, vertexConsumers, light, overlay);

        if (!(entity instanceof BinaryGraphicsRenderProvider renderProvider) || !renderProvider.canRender()) return;
        Direction facing = entity.getCachedState().get(Properties.HORIZONTAL_FACING);

        RenderLayer layer = RenderLayer.getCutout();
        VertexConsumer vc = vertexConsumers.getBuffer(layer);
        Sprite sprite;

        matrices.push();

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
        matrices.translate(-0.5, -0.5, -0.5);

        Matrix4f mat = matrices.peek().getPositionMatrix();
        if (renderProvider.getBinaryGraphics()!=null){
            sprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(Identifier.of("neetcomputers", "block/white"));
            BinaryGraphicsArray graphics = renderProvider.getBinaryGraphics();
            int sizex = graphics.getSize().x, sizey = graphics.getSize().y;
            for (int x = 0; x < sizex; x++){
                for (int y = 0; y < sizey; y++){
                    if (graphics.get(x,y)){
                        drawPixel(x,y,vc,mat,overlay,sprite, renderProvider, entity);
                    }
                }
            }
        }

        matrices.pop();
    }

    public VertexConsumer applyPos(VertexConsumer vc, Matrix4f mat, Matrix3f rot, Vector3f off,float x,float y,float z){
        Vector3f pos = new Vector3f(x,y,z).mul(rot).add(off);
        return vc.vertex(mat, pos.x, pos.y, pos.z);
    }

    public abstract Vector3f getOffset(BlockEntityType blockEntity);
    public abstract Matrix3f getRotation(BlockEntityType blockEntity);

    public void drawPixel(int x, int y, VertexConsumer vc, Matrix4f mat, int overlay, Sprite sprite, BinaryGraphicsRenderProvider entity, BlockEntityType blockEntityType){
        BinaryGraphicsArray graphics = entity.getBinaryGraphics();

        float x1 = x / 16f, x2 = 1f / 16f + x1;
        float y1 = y / 16f, y2 = 1f / 16f + y1;
        float z1 = -0.5f / 16f, z2 = -1f / 16f;

        Vector3f pos1 = new Vector3f(x1, y1, z1);
        Vector3f pos2 = new Vector3f(x2, y2, z2);

        Vec3i rgb = entity.getColoration(clock, x, y);

        applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos1.x, pos1.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos2.x, pos1.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos2.x, pos2.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos1.x, pos2.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        if (!graphics.get(x, y+1)){
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos1.x, pos2.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos2.x, pos2.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos2.x, pos2.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos1.x, pos2.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        }
        if (!graphics.get(x, y-1)){
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos2.x, pos1.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos1.x, pos1.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos1.x, pos1.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos2.x, pos1.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        }
        if (!graphics.get(x+1, y)){
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos2.x, pos2.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos2.x, pos1.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos2.x, pos1.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos2.x, pos2.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        }
        if (!graphics.get(x-1, y)){
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos1.x, pos1.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos1.x, pos2.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos1.x, pos2.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(blockEntityType), getOffset(blockEntityType), pos1.x, pos1.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        }
    }

    @Override
    public boolean rendersOutsideBoundingBox(BlockEntityType blockEntity) {
        return super.rendersOutsideBoundingBox(blockEntity);
    }
}
