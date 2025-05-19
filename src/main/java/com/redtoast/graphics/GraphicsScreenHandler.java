package com.redtoast.graphics;

import com.redtoast.neet.NeetComputers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;


public class GraphicsScreenHandler extends ScreenHandler {
    private RGBGraphicsArray graphics;

    public GraphicsScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(NeetComputers.GRAPHICS_SCREEN_HANDLER, syncId);
        int y = buf.readInt();
        int x = buf.readInt();
        int[][] arr = new int[y][x];
        for (int i=0; i < y; i++) {
            arr[i] = buf.readIntArray(x);
        }
        graphics = new RGBGraphicsArray(arr);
    }

    public GraphicsScreenHandler(int syncId, RGBGraphicsArray arr) {
        super(NeetComputers.GRAPHICS_SCREEN_HANDLER, syncId);
        graphics = arr;
    }

    public RGBGraphicsArray getGraphics() {
        return graphics;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

}
