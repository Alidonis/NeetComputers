package com.redtoast.graphics;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.joml.Vector2i;

public class GraphicsScreen extends HandledScreen<GraphicsScreenHandler> {
    private GraphicsScreenHandler handler;
    public GraphicsScreen(GraphicsScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RGBGraphicsArray graphics = handler.getGraphics();
        Vector2i size = graphics.getSize();
        int x1 = context.getScaledWindowWidth()/2 - size.x()*2;
        int y1 = context.getScaledWindowHeight()/2 - size.y()*2;;
        for (int x = 0; x < size.x(); x++) {
            for (int y = 0; y < size.y(); y++) {
                context.fill(x1+x*4,y1+y*4,x1+x*4+4,y1+y*4+4,0xFF000000 | graphics.get(x,y));
            }
        }
        context.draw();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {}
}
