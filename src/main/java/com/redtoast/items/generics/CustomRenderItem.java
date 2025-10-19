package com.redtoast.items.generics;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class CustomRenderItem extends Item {
    public CustomRenderItem(Settings settings) {
        super(settings);
    }

    public abstract void render(ItemStack stack,
                       ModelTransformationMode renderMode,
                       double deltaTime,
                       MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers,
                       int light,
                       int overlay,
                       BakedModel model,
                       Transformation transformation);
}
