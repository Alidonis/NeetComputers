package com.redtoast.blocks.Generics;

import com.redtoast.ComputerStatus;
import com.redtoast.graphics.BinaryGraphicsArray;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
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

public abstract class  ComputerRenderer<ComputerType extends ComputerBlockEntity> extends PipeSourceBlockRenderer<ComputerType> {
    float clock = 0;
    static int opacity = 255;
    public BlockState state;

    @Override
    public void render(ComputerType entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        clock %= 6.2f;
        BlockState state = entity.getCachedState();
        this.state = state;

        super.render(entity, tickDelta, matrices, vertexConsumers, light, overlay);

        if (state.get(ComputerBlock.STATE)==0) {
            return;
        }
        Direction facing = entity.getCachedState().get(Properties.HORIZONTAL_FACING);

        RenderLayer layer = RenderLayer.getCutout();
        VertexConsumer vc = vertexConsumers.getBuffer(layer);
        Sprite sprite;

        matrices.push();

        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
        matrices.translate(-0.5, -0.5, -0.5);

        Matrix4f mat = matrices.peek().getPositionMatrix();

        if (entity.getComputer().getBinaryGraphics()!=null){
            sprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(Identifier.of("neetcomputers", "block/white"));
            BinaryGraphicsArray graphics = entity.getComputer().getBinaryGraphics();
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

    public VertexConsumer applyPos(VertexConsumer vc, Matrix4f mat, Matrix3f rot, Vector3f off,float x,float y,float z){
        Vector3f pos = new Vector3f(x,y,z).mul(rot).add(off);
        return vc.vertex(mat, pos.x, pos.y, pos.z);
    }

    public abstract Vector3f getOffset();
    public abstract Matrix3f getRotation();

    public void drawPixel(int x, int y, VertexConsumer vc, Matrix4f mat, int overlay, Sprite sprite, ComputerBlockEntity entity){
        float x1 = x / 16f, x2 = 1f / 16f + x1;
        float y1 = y / 16f, y2 = 1f / 16f + y1;
        float z1 = -0.5f / 16f, z2 = -1f / 16f;

        Vector3f pos1 = new Vector3f(x1, y1, z1);
        Vector3f pos2 = new Vector3f(x2, y2, z2);

        Vec3i rgb = getPixel(x, y);
        BinaryGraphicsArray graphics = entity.getComputer().getBinaryGraphics();

        applyPos(vc, mat, getRotation(), getOffset(), pos1.x, pos1.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        applyPos(vc, mat, getRotation(), getOffset(), pos2.x, pos1.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        applyPos(vc, mat, getRotation(), getOffset(), pos2.x, pos2.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        applyPos(vc, mat, getRotation(), getOffset(), pos1.x, pos2.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        if (!graphics.get(x, y+1)){
            applyPos(vc, mat, getRotation(), getOffset(), pos1.x, pos2.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(), getOffset(), pos2.x, pos2.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(), getOffset(), pos2.x, pos2.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(), getOffset(), pos1.x, pos2.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        }
        if (!graphics.get(x, y-1)){
            applyPos(vc, mat, getRotation(), getOffset(), pos2.x, pos1.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(), getOffset(), pos1.x, pos1.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(), getOffset(), pos1.x, pos1.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(), getOffset(), pos2.x, pos1.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        }
        if (!graphics.get(x+1, y)){
            applyPos(vc, mat, getRotation(), getOffset(), pos2.x, pos2.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(), getOffset(), pos2.x, pos1.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(), getOffset(), pos2.x, pos1.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(), getOffset(), pos2.x, pos2.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        }
        if (!graphics.get(x-1, y)){
            applyPos(vc, mat, getRotation(), getOffset(), pos1.x, pos1.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(), getOffset(), pos1.x, pos2.y, pos1.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(), getOffset(), pos1.x, pos2.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
            applyPos(vc, mat, getRotation(), getOffset(), pos1.x, pos1.y, pos2.z).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1);
        }
    }

    public Vec3i getPixel(int x, int y){
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
        return switch (ComputerStatus.values()[state.get(ComputerBlock.STATE)]) {
            case OFF -> {
                if ((y%4>1 || x%4>1) && !(y%4>1 && x%4>1)) yield new Vec3i(201, 109, 233);
                yield new Vec3i(0, 0, 0);
            }
            case ON -> new Vec3i(r,g,b);
            case CRASHED -> new Vec3i(g, r, b);
            case PAUSED -> new Vec3i(b, r, g);
        };
    }

    @Override
    public boolean rendersOutsideBoundingBox(ComputerType blockEntity) {
        return super.rendersOutsideBoundingBox(blockEntity);
    }
}
