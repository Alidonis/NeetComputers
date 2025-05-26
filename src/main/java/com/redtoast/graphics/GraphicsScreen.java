package com.redtoast.graphics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.redtoast.neet.NeetComputers;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.joml.Vector2i;

public class GraphicsScreen extends HandledScreen<GraphicsScreenHandler> {
    private GraphicsScreenHandler handler;
    private Vector2i mousePos;
    public static int screenMult = 2;
    public GraphicsScreen(GraphicsScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
        mousePos = new Vector2i(0,0);
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RGBGraphicsArray graphics = handler.getGraphics();
        Vector2i size = graphics.getSize();
        int x1 = context.getScaledWindowWidth()/2 - size.x()*(screenMult/2);
        int y1 = context.getScaledWindowHeight()/2 - size.y()*(screenMult/2);
        int x2 = context.getScaledWindowWidth()/2 + size.x()*(screenMult/2);
        int y2 = context.getScaledWindowHeight()/2 + size.y()*(screenMult/2);
        boolean inBounds = true;
        if (mouseX > x2 || mouseY > y2) {
            inBounds = false;
        }
        mouseX -= x1;
        mouseY -= y1;
        mouseX = mouseX/screenMult;
        mouseY = mouseY/screenMult;
        if (mouseX < 0 || mouseY < 0) {
            inBounds = false;
        }
        if (!(mouseX == mousePos.x && mouseY == mousePos.y) && inBounds) {
            mousePos = new Vector2i(mouseX,mouseY);

        }
        Matrix4f transformationMatrix = context.getMatrices().peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        // Initialize the buffer using the specified format and draw mode.
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int x = 0; x < size.x(); x++) {
            for (int y = 0; y < size.y(); y++) {
                //context.fill(x1+x*4,y1+y*4,x1+x*4+4,y1+y*4+4,0xFF000000 | graphics.get(x,y));
                buffer.vertex(transformationMatrix, x1+x*screenMult, y1+y*screenMult, 9999).color(0xFF000000 | graphics.get(x,y)).next();
                buffer.vertex(transformationMatrix, x1+x*screenMult, y1+y*screenMult+screenMult, 9999).color(0xFF000000 | graphics.get(x,y)).next();
                buffer.vertex(transformationMatrix, x1+x*screenMult+screenMult, y1+y*screenMult, 9999).color(0xFF000000 | graphics.get(x,y)).next();
                buffer.vertex(transformationMatrix, x1+x*screenMult+screenMult, y1+y*screenMult+screenMult, 9999).color(0xFF000000 | graphics.get(x,y)).next();
            }
        }
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        tessellator.draw();
        context.draw();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RGBGraphicsArray graphics = handler.getGraphics();
        Vector2i size = graphics.getSize();
        int x1 = context.getScaledWindowWidth()/2 - size.x()*(screenMult/2);
        int y1 = context.getScaledWindowHeight()/2 - size.y()*(screenMult/2);
        int x2 = context.getScaledWindowWidth()/2 + size.x()*(screenMult/2);
        int y2 = context.getScaledWindowHeight()/2 + size.y()*(screenMult/2);
        context.fill(x1-screenMult,y1-screenMult,x2+screenMult,y2+screenMult,0xFFFFFFFF);
        context.draw();
    }
}
