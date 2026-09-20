package com.redtoast.graphics.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom render layer for screens.
 *
 * <p>Uses the {@code neetcomputers:screen_display} shader (3x nearest +
 * bilinear, fullbright, no fog, no mipmaps) with the screen's own texture.
 * Falls back to the vanilla position-tex-color program if the custom shader
 * has not loaded yet, so rendering never crashes during resource reloads.</p>
 */
@Environment(EnvType.CLIENT)
public class ScreenRenderLayers extends RenderLayer {
    private static final Map<Identifier, RenderLayer> CACHE = new ConcurrentHashMap<>();

    private ScreenRenderLayers(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode,
                               int expectedBufferSize, boolean hasCrumbling, boolean translucent,
                               Runnable startAction, Runnable endAction) {
        super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
    }

    private static RenderLayer create(Identifier texture) {
        RenderLayer.MultiPhaseParameters params = RenderLayer.MultiPhaseParameters.builder()
                .texture(new RenderPhase.Texture(texture, false, false))
                .program(new RenderPhase.ShaderProgram(() -> {
                    var custom = ScreenShaders.getDisplayProgram();
                    return custom != null ? custom : GameRenderer.getPositionTexColorProgram();
                }))
                .transparency(NO_TRANSPARENCY)
                .lightmap(DISABLE_LIGHTMAP)
                .overlay(DISABLE_OVERLAY_COLOR)
                .cull(DISABLE_CULLING)
                .depthTest(LEQUAL_DEPTH_TEST)
                .writeMaskState(ALL_MASK)
                .build(false);
        return of("neet_display", VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS,
                256, false, false, params);
    }

    public static RenderLayer getDisplay(Identifier texture) {
        return CACHE.computeIfAbsent(texture, ScreenRenderLayers::create);
    }
}
