package com.redtoast.graphics;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.joml.Vector2i;

public class GraphicsScreen extends HandledScreen<GraphicsScreenHandler> {
    private RGBGraphicsArray graphics;
    public GraphicsScreen(GraphicsScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        graphics = handler.getGraphics();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        Vector2i size = graphics.getSize();
        for (int x = 0; x < size.x(); x++) {
            for (int y = 0; y < size.y(); y++) {
                context.fill(x*3,y*3,x*3+3,y*3+3,0xFF000000 | graphics.get(x,y));
            }
        }
        context.draw();
    }
}
