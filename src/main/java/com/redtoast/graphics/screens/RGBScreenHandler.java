package com.redtoast.graphics.screens;

import com.redtoast.Computer;
import com.redtoast.graphics.RGBGraphicsArray;
import com.redtoast.graphics.SectoredGraphics;
import com.redtoast.neet.NeetComputersServer;
import com.redtoast.neet.Networking.ComputerScreenInitPayload;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

import java.util.UUID;


public class RGBScreenHandler extends ScreenHandler {
    private SectoredGraphics graphics;
    public Computer comp;
    public UUID uuid;

    public RGBScreenHandler(int syncId, PlayerInventory playerInventory, ComputerScreenInitPayload payload) {
        super(NeetComputersServer.GRAPHICS_SCREEN_HANDLER, syncId);
        graphics = (SectoredGraphics) payload.graphics();
        uuid = payload.uuid();
    }

    public RGBScreenHandler(int syncId, RGBGraphicsArray arr, Computer a) {
        super(NeetComputersServer.GRAPHICS_SCREEN_HANDLER, syncId);
        comp = a;
        uuid = a.getUuid();
    }

    public void updateGraphics(SectoredGraphics graphics) {
        this.graphics = graphics;
    }

    public SectoredGraphics getGraphics() {
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
