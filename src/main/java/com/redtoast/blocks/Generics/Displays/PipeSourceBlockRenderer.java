package com.redtoast.blocks.Generics.Displays;

import com.redtoast.Connections.CableRenderer;
import com.redtoast.Connections.PipeRenderSource;
import com.redtoast.items.generics.ConnectorItem;
import com.redtoast.items.generics.DisplayPipes;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class PipeSourceBlockRenderer<Type extends BlockEntity> implements BlockEntityRenderer<Type> {
    public PipeSourceBlockRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(Type entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity instanceof PipeRenderSource renderSource && !CableRenderer.doesBlockExist(entity.getPos())){
            assert MinecraftClient.getInstance().player != null;
            boolean isholding = false;
            DisplayPipes connectorItem2 = null;
            if (MinecraftClient.getInstance().player.getOffHandStack().getItem() instanceof DisplayPipes connectorItem){
                isholding = true;
                connectorItem2 = connectorItem;
            }else if (MinecraftClient.getInstance().player.getMainHandStack().getItem() instanceof DisplayPipes connectorItem){
                isholding = true;
                connectorItem2 = connectorItem;
            }
            if (isholding && renderSource.shouldRenderPipeType(connectorItem2.getType())) {
                CableRenderer.drawPipeBlock(matrices, vertexConsumers, entity.getPos().toCenterPos().add(-0.5, -0.5, -0.5), entity.getPos(), entity.getWorld(), connectorItem2.getType().getSourceTexture());
            }
        }
    }
}
