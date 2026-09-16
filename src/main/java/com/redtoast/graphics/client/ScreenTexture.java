package com.redtoast.graphics.client;

import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.graphics.SectoredGraphics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * GPU texture holding a 1:1 copy of a screen's pixels.
 *
 * <p>The old rectangle/sector rendering drew one quad per sector, which caused
 * cracks, missing pixels and half-drawn pixels (floating point seams,
 * rasterization rules and block-atlas mipmaps). This class uploads the pixels
 * once into a dedicated texture with mipmaps disabled; the custom
 * {@code screen_display} shader then performs the 3x nearest-neighbor upscale
 * plus bilinear downscale on the GPU when a single quad is drawn.</p>
 */
@Environment(EnvType.CLIENT)
public final class ScreenTexture implements AutoCloseable {
    private final Identifier id;
    private NativeImageBackedTexture texture;
    private int width = -1;
    private int height = -1;
    private boolean registered = false;

    public ScreenTexture() {
        this.id = Identifier.of("neetcomputers", "dynamic/screen_" + UUID.randomUUID().toString().replace("-", ""));
    }

    public ScreenTexture(String name) {
        this.id = Identifier.of("neetcomputers", "dynamic/screen_" + name + "_" + UUID.randomUUID().toString().replace("-", ""));
    }

    public Identifier getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private void ensureSize(int w, int h) {
        if (texture != null && width == w && height == h) {
            return;
        }
        if (texture != null) {
            try {
                texture.close();
            } catch (Exception ignored) {
            }
            registered = false;
            texture = null;
        }
        width = w;
        height = h;
        texture = new NativeImageBackedTexture(w, h, false);
        texture.setFilter(false, false);
        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
        registered = true;
        texture.setFilter(false, false);
    }

    private static int argbToAbgr(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    /**
     * Uploads sectored graphics by rasterizing sectors directly into the image.
     * Sectors are used as-is (no rectangle rendering); each sector fills its
     * pixel range, so adjacent sectors can never leave seams.
     *
     * @return true if the texture now holds {@code graphics}
     */
    public boolean upload(SectoredGraphics graphics) {
        if (graphics == null) {
            return false;
        }
        int w = graphics.size().x;
        int h = graphics.size().y;
        if (w <= 0 || h <= 0) {
            return false;
        }
        ensureSize(w, h);
        NativeImage image = texture.getImage();
        if (image == null) {
            return false;
        }
        int black = argbToAbgr(0xFF000000);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                image.setColor(x, y, black);
            }
        }
        for (SectoredGraphics.Sector sector : graphics) {
            int color = argbToAbgr(sector.color());
            int x1 = Math.max(0, sector.x1());
            int y1 = Math.max(0, sector.y1());
            int x2 = Math.min(w - 1, sector.x2());
            int y2 = Math.min(h - 1, sector.y2());
            for (int y = y1; y <= y2; y++) {
                for (int x = x1; x <= x2; x++) {
                    image.setColor(x, y, color);
                }
            }
        }
        texture.setFilter(false, false);
        texture.upload();
        texture.setFilter(false, false);
        return true;
    }

    /**
     * Uploads a raw RGB array (kept for completeness / future use).
     */
    public boolean upload(RGBGraphicsArray graphics) {
        if (graphics == null) {
            return false;
        }
        int w = graphics.getSize().x;
        int h = graphics.getSize().y;
        if (w <= 0 || h <= 0) {
            return false;
        }
        ensureSize(w, h);
        NativeImage image = texture.getImage();
        if (image == null) {
            return false;
        }
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                image.setColor(x, y, argbToAbgr(graphics.get(x, y) | 0xFF000000));
            }
        }
        texture.setFilter(false, false);
        texture.upload();
        texture.setFilter(false, false);
        return true;
    }

    @Override
    public void close() {
        if (texture != null) {
            if (registered) {
                try {
                    MinecraftClient.getInstance().getTextureManager().destroyTexture(id);
                } catch (Exception ignored) {
                }
                registered = false;
            }
            try {
                texture.close();
            } catch (Exception ignored) {
            }
            texture = null;
        }
        width = -1;
        height = -1;
    }
}
