package com.redtoast.mixin.client;

import com.redtoast.graphics.screens.RGBScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin( value = InGameHud.class)
public class InGameHudMixin {
    @Shadow private @Nullable Text overlayMessage;

    @Unique
    private static void logic(CallbackInfo ci){
        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.currentScreenHandler instanceof RGBScreenHandler) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void onHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
        logic(ci);
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void onExperience(DrawContext context, int x, CallbackInfo ci){
        logic(ci);
    }

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void renderStatusBars(DrawContext context, CallbackInfo ci){
        logic(ci);
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void renderStatusEffectOverlay(DrawContext context, CallbackInfo ci){
        logic(ci);
    }
}
