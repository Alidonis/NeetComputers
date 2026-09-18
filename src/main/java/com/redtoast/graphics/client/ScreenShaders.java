package com.redtoast.graphics.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

/**
 * Holds the custom screen shader program.
 *
 * <p>The program is loaded from
 * {@code assets/neetcomputers/shaders/core/screen_display.json} and performs
 * the 3x nearest-neighbor upscale followed by a bilinear downscale entirely
 * on the GPU (see the .fsh for details).</p>
 */
@Environment(EnvType.CLIENT)
public final class ScreenShaders {
    private static volatile ShaderProgram displayProgram;

    private ScreenShaders() {
    }

    public static void setDisplayProgram(ShaderProgram program) {
        displayProgram = program;
    }

    public static ShaderProgram getDisplayProgram() {
        return displayProgram;
    }

    public static void register() {
        CoreShaderRegistrationCallback.EVENT.register(context -> context.register(
                Identifier.of("neetcomputers", "screen_display"),
                VertexFormats.POSITION_TEXTURE_COLOR,
                ScreenShaders::setDisplayProgram));
    }
}
