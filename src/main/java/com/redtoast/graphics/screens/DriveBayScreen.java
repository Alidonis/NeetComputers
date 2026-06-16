package com.redtoast.graphics.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DriveBayScreen extends HandledScreen<DriveBayScreenHandler> {
    public DriveBayScreen(DriveBayScreenHandler container, PlayerInventory player, Text title) {
        super(container, player, title);
    }

    protected void drawBackground(DrawContext graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.drawTexture(Identifier.of("neetcomputers", "textures/gui/drive_bay.png"), x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(graphics, mouseX, mouseY);
    }
}
