package com.redtoast.graphics.client;

import com.redtoast.blocks.ColorDisplay.ColorDisplayBlockEntity;
import com.redtoast.graphics.SectoredGraphics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache of per-display textures for color displays.
 *
 * <p>Kept out of the block entity on purpose: the entity class also exists on
 * dedicated servers, where client texture classes must never load.</p>
 */
@Environment(EnvType.CLIENT)
public final class DisplayTextureManager {
    private record Entry(ScreenTexture texture, SectoredGraphics graphics) {
    }

    private static final Map<BlockPos, Entry> TEXTURES = new ConcurrentHashMap<>();

    private DisplayTextureManager() {
    }

    /**
     * Returns the texture for {@code pos}, uploading {@code graphics} if it
     * changed since the last call. Must be called on the render thread.
     */
    public static ScreenTexture get(BlockPos pos, SectoredGraphics graphics) {
        if (pos == null || graphics == null) {
            return null;
        }
        BlockPos key = pos.toImmutable();
        Entry entry = TEXTURES.get(key);
        if (entry != null && entry.graphics() == graphics) {
            return entry.texture();
        }
        ScreenTexture texture = entry != null ? entry.texture() : new ScreenTexture("display");
        if (!texture.upload(graphics)) {
            if (entry == null) {
                texture.close();
            }
            return null;
        }
        TEXTURES.put(key, new Entry(texture, graphics));
        return texture;
    }

    public static void remove(BlockPos pos) {
        if (pos == null) {
            return;
        }
        Entry entry = TEXTURES.remove(pos.toImmutable());
        if (entry != null) {
            entry.texture().close();
        }
    }

    /**
     * Drops textures whose block entity is gone (block broken / chunk
     * unloaded). Called periodically from the client tick.
     */
    public static void purge() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            for (BlockPos key : new ArrayList<>(TEXTURES.keySet())) {
                remove(key);
            }
            return;
        }
        for (BlockPos key : new ArrayList<>(TEXTURES.keySet())) {
            if (!(client.world.getBlockEntity(key) instanceof ColorDisplayBlockEntity)) {
                remove(key);
            }
        }
    }
}
