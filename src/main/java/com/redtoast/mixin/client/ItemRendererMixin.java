package com.redtoast.mixin.client;

import com.redtoast.items.generics.ComputerItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MatrixUtil;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    private long tickTime = System.currentTimeMillis();
    @Inject(
            method = "renderItem*",
            at = @At("TAIL"))
    private void renderAfterOriginal(ItemStack stack,
             ModelTransformationMode renderMode,
             boolean leftHanded,
             MatrixStack matrices,
             VertexConsumerProvider vertexConsumers,
             int light,
             int overlay,
             BakedModel model,
             CallbackInfo ci
    ) {
        double delta = (double) (System.currentTimeMillis() - tickTime) / 1000d;
        tickTime = System.currentTimeMillis();
        if (!stack.isEmpty() && stack.getItem() instanceof ComputerItem computerItem){
            ModelTransformation modelTransform = model.getTransformation();

            Transformation modeTransform = modelTransform.getTransformation(renderMode);

            matrices.push();

            modeTransform.apply(leftHanded, matrices);

            matrices.multiply(new Quaternionf().rotateX((float) -Math.PI / 2));

            computerItem.render(
                    stack,
                    renderMode,
                    delta,
                    matrices,
                    vertexConsumers,
                    light,
                    overlay,
                    model,
                    modeTransform
            );

            matrices.pop();
        }
    }
}
