package com.redtoast.items;

import com.redtoast.computerSpecs;
import com.redtoast.graphics.BinaryGraphicsArray;
import com.redtoast.items.generics.ComputerItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import org.joml.Matrix4f;

public class mobileComputer{} /** extends ComputerItem {
    double clock;
    static int opacity = 255;
    public mobileComputer(Settings settings) {
        super(settings, new computerSpecs()
                .setBinaryGraphicsSize(5, 6)
                .setColorGraphicsSize(81,108)
                .setIPS(130000)
                .setMachineName("Portable Computer")
        );
    }

    @Override
    public void render(ItemStack stack,
                       ModelTransformationMode renderMode,
                       double tickDelta,
                       MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers,
                       int light,
                       int overlay,
                       BakedModel model,
                       Transformation transformation)
    {
//        MinecraftClient client = MinecraftClient.getInstance();
//        if (!client.isPaused() && client.isInSingleplayer()){
//            clock += tickDelta * 0.01d;
//        }
//        clock %= 6.2f;
//
//        if (!getComputer().isOn() && !getComputer().isCrashed()) {
//            return;
//        }
//
//        RenderLayer layer = RenderLayer.getCutout();
//        VertexConsumer vc = vertexConsumers.getBuffer(layer);
//        Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(new Identifier("neetcomputers", "block/front_on_uv"));
//
//        Matrix4f mat = matrices.peek().getPositionMatrix();
//
//        float z =  0f / 16f;
//        float x1 = 0.5f / 16f, x2 = 2f / 16f + x1;
//        float y1 = 3f / 16f, y2 = 1f / 16f + y1;
//
//        vc.vertex(mat, x1, y1, z).color(255, 255, 255, opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//        vc.vertex(mat, x2, y1, z).color(255, 255, 255, opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//        vc.vertex(mat, x2, y2, z).color(255, 255, 255, opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//        vc.vertex(mat, x1, y2, z).color(255, 255, 255, opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//
//        if (getComputer().getBinaryGraphics()!=null){
//            sprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(new Identifier("neetcomputers", "block/white"));
//            BinaryGraphicsArray graphics = getComputer().getBinaryGraphics();
//            int sizex = graphics.getSize().x, sizey = graphics.getSize().y;
//            for (int x = 0; x < sizex; x++){
//                for (int y = 0; y < sizey; y++){
//                    if (graphics.get(x,y)){
//                        drawPixel(x,y,vc,mat,overlay,sprite);
//                    }
//                }
//            }
//        }


    }

//    private void drawPixel(int x, int y, VertexConsumer vc, Matrix4f mat, int overlay, Sprite sprite){
//        float x1 = x / 16f, x2 = 1f / 16f + x1;
//        float y1 = y / 16f, y2 = 1f / 16f + y1;
//        float z1 = -0.5f / 16f, z2 = -1f / 16f;
//        float offsetX = 0.5f / 16f, offsetY = 1f / 16f;
//        Vec3i rgb = getPixel(x, y);
//        BinaryGraphicsArray graphics = getComputer().getBinaryGraphics();
//        vc.vertex(mat, x1 + offsetX, y1 + offsetY, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//        vc.vertex(mat, x2 + offsetX, y1 + offsetY, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//        vc.vertex(mat, x2 + offsetX, y2 + offsetY, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//        vc.vertex(mat, x1 + offsetX, y2 + offsetY, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//        x1 += offsetX;
//        x2 += offsetX;
//        y1 += offsetY;
//        y2 += offsetY;
//        if (!graphics.get(x, y+1)){
//            vc.vertex(mat, x1, y2, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//            vc.vertex(mat, x2, y2, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//            vc.vertex(mat, x2, y2, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//            vc.vertex(mat, x1, y2, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//        }
//        if (!graphics.get(x, y-1)){
//            vc.vertex(mat, x2, y1, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//            vc.vertex(mat, x1, y1, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//            vc.vertex(mat, x1, y1, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//            vc.vertex(mat, x2, y1, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//        }
//        if (!graphics.get(x+1, y)){
//            vc.vertex(mat, x2, y2, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//            vc.vertex(mat, x2, y1, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//            vc.vertex(mat, x2, y1, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//            vc.vertex(mat, x2, y2, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//        }
//        if (!graphics.get(x-1, y)){
//            vc.vertex(mat, x1, y1, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//            vc.vertex(mat, x1, y2, z1).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//            vc.vertex(mat, x1, y2, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMinU(), sprite.getMinV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//            vc.vertex(mat, x1, y1, z2).color(rgb.getX(), rgb.getY(), rgb.getZ(), opacity).texture(sprite.getMaxU(), sprite.getMaxV()).light(0xF000F0).overlay(overlay).normal(0, 0, -1).next();
//        }
//    }
//
//    private Vec3i getPixel(int x, int y){
//        int r = 40;
//        int g = 226;
//        int b = 50;
//        if ((x+2)%3==0){
//            r++;
//            g += 8;
//            b += 3;
//        }
//        if ((y+1)%2==0){
//            r++;
//            g += 10;
//            b += 3;
//        }
//        double density = 0.4f;
//        double offset = (clock + y * density) % 6.2d;
//        double weight = 8;
//        double effect = Math.sin(offset) * weight;
//        r += (int)Math.round(effect);
//        g += (int)Math.round(effect);
//        b += (int)Math.round(effect);
//        return !getComputer().isCrashed() ? new Vec3i(r,g,b) : new Vec3i(g, r, b);
//    }
}**/