package com.redtoast.graphics;

import com.redtoast.neet.NeetComputers;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Vector2i;

public class GraphicsScreen extends HandledScreen<GraphicsScreenHandler> {
    private GraphicsScreenHandler handler;
    private Vector2i mousePos;
    public GraphicsScreen(GraphicsScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
        mousePos = new Vector2i(0,0);
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RGBGraphicsArray graphics = handler.getGraphics();
        Vector2i size = graphics.getSize();
        int x1 = context.getScaledWindowWidth()/2 - size.x()*2;
        int y1 = context.getScaledWindowHeight()/2 - size.y()*2;
        int x2 = context.getScaledWindowWidth()/2 + size.x()*2;
        int y2 = context.getScaledWindowHeight()/2 + size.y()*2;
        if (mouseX > x2) {
            mouseX = x2;
        }
        if (mouseY > y2) {
            mouseY = y2;
        }
        mouseX -= x1;
        mouseY -= y1;
        mouseX = mouseX/4;
        mouseY = mouseY/4;
        if (mouseX < 0) {
            mouseX = 0;
        }
        if (mouseY < 0) {
            mouseY = 0;
        }
        if (!(mouseX == mousePos.x && mouseY == mousePos.y)) {
            mousePos = new Vector2i(mouseX,mouseY);
            PacketByteBuf a = PacketByteBufs.create();
            a.writeInt(mouseX);
            a.writeInt(mouseY);
            ClientPlayNetworking.send(NeetComputers.MOUSE_MOVE_PACKET_ID,a);
        }
        for (int x = 0; x < size.x(); x++) {
            for (int y = 0; y < size.y(); y++) {
                context.fill(x1+x*4,y1+y*4,x1+x*4+4,y1+y*4+4,0xFF000000 | graphics.get(x,y));
            }
        }
        context.draw();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.draw();
    }
}
