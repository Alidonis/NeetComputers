package com.redtoast.Connections;

import com.redtoast.items.generics.ConnectorItem;
import com.redtoast.neet.NeetComputersClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.Hashtable;

@Environment(EnvType.CLIENT)
public class CableRenderer {

    public static void eventCallback(WorldRenderContext context){
        PlayerEntity mainPlayer = MinecraftClient.getInstance().player;
        if (mainPlayer!=null && mainPlayer.getMainHandStack().getItem() instanceof ConnectorItem connectorItem){
            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider vertexConsumers = context.consumers();
            Camera camera = context.camera();
            World world = context.world();

            if (NeetComputersClient.lastTypeSent != connectorItem.getType()) return;
            BlockPos[] positions = NeetComputersClient.positionsForPipeRendering;
            for (BlockPos pos : positions) {
                if (!world.getBlockState(pos).isAir()) CableRenderer.drawPipeBlock(matrices, vertexConsumers, camera, pos, world, connectorItem.getType(), connectorItem.getType().getTexture());
            }
        }
    }

    public static boolean doesBlockExist(BlockPos pos){
        for (BlockPos pos2 : NeetComputersClient.positionsForPipeRendering) if (pos.equals(pos2)) return true;
        return false;
    }

    public static void drawPipeBlock(
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            Camera camera,
            BlockPos targetPos,
            World world,
            PipeType pipeType,
            Identifier texture
    ) {
        Vec3d cameraPos = camera.getPos();
        double x = targetPos.getX() - cameraPos.x;
        double y = targetPos.getY() - cameraPos.y;
        double z = targetPos.getZ() - cameraPos.z;
        matrices.push();

        Hashtable<Direction, Boolean> neighborMap = new Hashtable<>();
        for (Direction direction : Direction.values()) {
            neighborMap.put(direction, !doesBlockExist(targetPos.offset(direction)));
        }

        matrices.translate(x, y, z);

        renderPipe(matrices, vertexConsumers, texture, neighborMap);
        matrices.pop();
    }

    public static void renderPipe(MatrixStack matrices, VertexConsumerProvider vertexConsumers, Identifier texture, Hashtable<Direction, Boolean> neighborMap) {
        RenderLayer layer = RenderLayer.getCutout();
        VertexConsumer vc = vertexConsumers.getBuffer(layer);
        Sprite sprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(texture);

        MatrixStack.Entry entry = matrices.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();

        float upValue = 1.01f;
        float downValue = -0.01f;
        int lightLevel = 15728880;
        int alpha = 255;

        float u0 = sprite.getMinU();
        float u1 = sprite.getMaxU();
        float v1 = sprite.getMinV();
        float v0 = sprite.getMaxV();


        if (neighborMap.get(Direction.SOUTH)) {
            vc.vertex(positionMatrix, upValue, upValue, upValue).color(255, 255, 255, alpha).texture(u1, v1).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, 0, -1);
            vc.vertex(positionMatrix, downValue, upValue, upValue).color(255, 255, 255, alpha).texture(u0, v1).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, 0, -1);
            vc.vertex(positionMatrix, downValue, downValue, upValue).color(255, 255, 255, alpha).texture(u0, v0).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, 0, -1);
            vc.vertex(positionMatrix, upValue, downValue, upValue).color(255, 255, 255, alpha).texture(u1, v0).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, 0, -1);
        }

        if (neighborMap.get(Direction.NORTH)) {
            vc.vertex(positionMatrix, downValue, upValue, downValue).color(255, 255, 255, alpha).texture(u1, v1).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, 0, 1);
            vc.vertex(positionMatrix, upValue, upValue, downValue).color(255, 255, 255, alpha).texture(u0, v1).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, 0, 1);
            vc.vertex(positionMatrix, upValue, downValue, downValue).color(255, 255, 255, alpha).texture(u0, v0).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, 0, 1);
            vc.vertex(positionMatrix, downValue, downValue, downValue).color(255, 255, 255, alpha).texture(u1, v0).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, 0, 1);
        }

        if (neighborMap.get(Direction.EAST)) {
            vc.vertex(positionMatrix, upValue, upValue, downValue).color(255, 255, 255, alpha).texture(u1, v1).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, -1, 0, 0);
            vc.vertex(positionMatrix, upValue, upValue, upValue).color(255, 255, 255, alpha).texture(u0, v1).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, -1, 0, 0);
            vc.vertex(positionMatrix, upValue, downValue, upValue).color(255, 255, 255, alpha).texture(u0, v0).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, -1, 0, 0);
            vc.vertex(positionMatrix, upValue, downValue, downValue).color(255, 255, 255, alpha).texture(u1, v0).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, -1, 0, 0);
        }

        if (neighborMap.get(Direction.WEST)) {
            vc.vertex(positionMatrix, downValue, upValue, upValue).color(255, 255, 255, alpha).texture(u1, v1).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 1, 0, 0);
            vc.vertex(positionMatrix, downValue, upValue, downValue).color(255, 255, 255, alpha).texture(u0, v1).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 1, 0, 0);
            vc.vertex(positionMatrix, downValue, downValue, downValue).color(255, 255, 255, alpha).texture(u0, v0).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 1, 0, 0);
            vc.vertex(positionMatrix, downValue, downValue, upValue).color(255, 255, 255, alpha).texture(u1, v0).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 1, 0, 0);
        }

        if (neighborMap.get(Direction.UP)) {
            vc.vertex(positionMatrix, upValue, upValue, downValue).color(255, 255, 255, alpha).texture(u1, v1).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, -1, 0);
            vc.vertex(positionMatrix, downValue, upValue, downValue).color(255, 255, 255, alpha).texture(u0, v1).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, -1, 0);
            vc.vertex(positionMatrix, downValue, upValue, upValue).color(255, 255, 255, alpha).texture(u0, v0).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, -1, 0);
            vc.vertex(positionMatrix, upValue, upValue, upValue).color(255, 255, 255, alpha).texture(u1, v0).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, -1, 0);
        }

        if (neighborMap.get(Direction.DOWN)) {
            vc.vertex(positionMatrix, upValue, downValue, upValue).color(255, 255, 255, alpha).texture(u1, v1).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, 1, 0);
            vc.vertex(positionMatrix, downValue, downValue, upValue).color(255, 255, 255, alpha).texture(u0, v1).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, 1, 0);
            vc.vertex(positionMatrix, downValue, downValue, downValue).color(255, 255, 255, alpha).texture(u0, v0).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, 1, 0);
            vc.vertex(positionMatrix, upValue, downValue, downValue).color(255, 255, 255, alpha).texture(u1, v0).overlay(OverlayTexture.DEFAULT_UV).light(lightLevel).normal(entry, 0, 1, 0);
        }
    }
}
