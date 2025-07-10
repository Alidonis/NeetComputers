package com.redtoast.graphics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.redtoast.simulation.EventGeneric;
import com.redtoast.simulation.value.Value;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.Hashtable;

public class GraphicsScreen extends HandledScreen<GraphicsScreenHandler> {
    public GraphicsScreenHandler handler;
    private Vector2i mousePos;
    public static int screenMult = 2;
    public static final Identifier TEXTURE = new Identifier("neetcomputers", "textures/gui/computer.png");
    private Hashtable<Integer, Vector2i> dragTable = new Hashtable<>();
    private Hashtable<Integer, Boolean> repeatTable = new Hashtable<>();
    private ClientPlayerEntity player;
    public GraphicsScreen(GraphicsScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
        mousePos = new Vector2i(0,0);
        RGBGraphicsArray graphics = handler.getGraphics();
        Vector2i size = graphics.getSize();
        this.backgroundWidth = size.x() * screenMult + 4;
        this.backgroundHeight = size.y() * screenMult + 4;
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
        assert inventory.player instanceof ClientPlayerEntity;
        player = (ClientPlayerEntity) inventory.player;
    }

    //i am boilerplate, destroyer of file size
    @Override
    public boolean keyPressed(int keycode, int scancode, int modifiers){
        EventGeneric event = new EventGeneric("keyPressed",
                Value.of(keycode),
                Value.of(GLFW.glfwGetKeyName(keycode, scancode)),
                Value.of(modifiers)
        );
        event.send(handler);
        return super.keyPressed(keycode, scancode, modifiers);
    }
    @Override
    public boolean keyReleased(int keycode, int scancode, int modifiers){
        EventGeneric event = new EventGeneric("keyReleased",
                Value.of(keycode),
                Value.of(GLFW.glfwGetKeyName(keycode, scancode)),
                Value.of(modifiers)
        );
        event.send(handler);
        return super.keyPressed(keycode, scancode, modifiers);
    }
    public Vector2i screenToGame(double screenX, double screenY) {
        Vector2i size = handler.getGraphics().getSize(); // width, height of the array

        int halfWidth = size.x() * screenMult / 2;
        int halfHeight = size.y() * screenMult / 2;

        Window window = MinecraftClient.getInstance().getWindow();
        int x1 = window.getScaledWidth() / 2 - halfWidth;
        int y1 = window.getScaledHeight() / 2 - halfHeight;
        int x2 = window.getScaledWidth() / 2 + halfWidth;
        int y2 = window.getScaledHeight() / 2 + halfHeight;

        if (screenX < x1 || screenX >= x2 || screenY < y1 || screenY >= y2) {
            return null;
        }

        int localX = (int) Math.floor((screenX - x1) / screenMult);
        int localY = (int) Math.floor((screenY - y1) / screenMult);

        if (localX < 0 || localX >= size.x() || localY < 0 || localY >= size.y()) {
            return null;
        }

        return new Vector2i(localX, localY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int key){
        Vector2i pos = screenToGame(mouseX, mouseY);
        if (pos!=null){
            if (!repeatTable.containsKey(key)) repeatTable.put(key, false);
            dragTable.put(key, pos);
            EventGeneric event = new EventGeneric("mouseClicked",
                    Value.of(pos.x),
                    Value.of(pos.y),
                    Value.of(key),
                    Value.of(!repeatTable.get(key))
            );
            event.send(handler);
            repeatTable.put(key, true);
        }else{
            dragTable.remove(key);
        }
        return super.mouseClicked(mouseX, mouseY, key);
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int key, double dragX, double dragY){
        Vector2i pos = screenToGame(mouseX, mouseY);
        if (pos!=null){
            if (!dragTable.containsKey(key)) {
                if (!repeatTable.containsKey(key)) repeatTable.put(key, false);
                new EventGeneric("mouseClicked",
                        Value.of(pos.x),
                        Value.of(pos.y),
                        Value.of(key),
                        Value.of(!repeatTable.get(key))
                ).send(handler);
                repeatTable.put(key, true);
            }
            if (!dragTable.containsKey(key) || !dragTable.get(key).equals(pos)){
                EventGeneric event = new EventGeneric("mouseDragged",
                        Value.of(pos.x),
                        Value.of(pos.y),
                        Value.of(key)
                );
                event.send(handler);
            }
            dragTable.put(key, pos);
        }else{
            if (dragTable.containsKey(key)){
                pos = dragTable.get(key);
                repeatTable.put(key, false);
                new EventGeneric("mouseReleased",
                        Value.of(pos.x),
                        Value.of(pos.y),
                        Value.of(key)
                ).send(handler);
            }
            dragTable.remove(key);
        }
        return super.mouseDragged(mouseX, mouseY, key, dragX, dragY);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int key){
        Vector2i pos = screenToGame(mouseX, mouseY);
        if (pos!=null){
            repeatTable.put(key, false);
            EventGeneric event = new EventGeneric("mouseReleased",
                    Value.of(pos.x),
                    Value.of(pos.y),
                    Value.of(key)
            );
            event.send(handler);
        }else{
            dragTable.remove(key);
        }
        return super.mouseReleased(mouseX, mouseY, key);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll){
        Vector2i pos = screenToGame(mouseX, mouseY);
        if (pos!=null){
            EventGeneric event = new EventGeneric("mouseScrolled",
                    Value.of(pos.x),
                    Value.of(pos.y),
                    Value.of(scroll)
            );
            event.send(handler);
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        RGBGraphicsArray graphics = handler.getGraphics();
        Vector2i size = graphics.getSize();
        int x1 = context.getScaledWindowWidth()/2 - size.x()*(screenMult/2);
        int y1 = context.getScaledWindowHeight()/2 - size.y()*(screenMult/2);
        int x2 = context.getScaledWindowWidth()/2 + size.x()*(screenMult/2);
        int y2 = context.getScaledWindowHeight()/2 + size.y()*(screenMult/2);
        context.fill(x1-screenMult*2,y1-screenMult*2,x2+screenMult*2,y2+screenMult*2,0xFFc6c6c6);
        context.fill(x1-screenMult,y1-screenMult,x2+screenMult,y2+screenMult,0xFFFFFFFF);
        context.draw();
    }

    @Override
    public void renderBackground(DrawContext context){

    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        renderBackground(context);
        RGBGraphicsArray graphics = handler.getGraphics();
        Vector2i size = graphics.getSize();
        int x1 = context.getScaledWindowWidth()/2 - size.x()*(screenMult/2);
        int y1 = context.getScaledWindowHeight()/2 - size.y()*(screenMult/2);
        int x2 = context.getScaledWindowWidth()/2 + size.x()*(screenMult/2);
        int y2 = context.getScaledWindowHeight()/2 + size.y()*(screenMult/2);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, TEXTURE);

        boolean inBounds = mouseX <= x2 && mouseY <= y2;
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
                buffer.vertex(transformationMatrix, x1+x*screenMult, y1+y*screenMult, 1).color(0xFF000000 | graphics.get(x,y)).next();
                buffer.vertex(transformationMatrix, x1+x*screenMult, y1+y*screenMult+screenMult, 1).color(0xFF000000 | graphics.get(x,y)).next();
                buffer.vertex(transformationMatrix, x1+x*screenMult+screenMult, y1+y*screenMult, 1).color(0xFF000000 | graphics.get(x,y)).next();
                buffer.vertex(transformationMatrix, x1+x*screenMult+screenMult, y1+y*screenMult+screenMult, 1).color(0xFF000000 | graphics.get(x,y)).next();
            }
        }
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        tessellator.draw();
        context.draw();
    }
}
