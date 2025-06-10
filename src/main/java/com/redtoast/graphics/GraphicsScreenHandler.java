package com.redtoast.graphics;

import com.redtoast.Computer;
import com.redtoast.neet.NeetComputers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;


public class GraphicsScreenHandler extends ScreenHandler {
    private RGBGraphicsArray graphics;
    public Computer comp;

    public GraphicsScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(NeetComputers.GRAPHICS_SCREEN_HANDLER, syncId);
        graphics = RGBGraphicsArray.fromPacket(buf);
    }

    public GraphicsScreenHandler(int syncId, RGBGraphicsArray arr, Computer a) {
        super(NeetComputers.GRAPHICS_SCREEN_HANDLER, syncId);
        graphics = arr;
        comp = a;
    }

    public void updateGraphics(RGBGraphicsArray graphics) {
        this.graphics = graphics;
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
