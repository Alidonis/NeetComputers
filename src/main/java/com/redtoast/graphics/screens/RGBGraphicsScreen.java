package com.redtoast.graphics.screens;

import com.redtoast.graphics.SectoredGraphics;
import com.redtoast.graphics.client.ScreenShaders;
import com.redtoast.graphics.client.ScreenTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2i;

public class RGBGraphicsScreen extends BoilerplateScreen {

    /*allocates the space (in percents) that the screen will be fit into (not including the border)*/
    protected double horizontalBufferSpace = 85;
    protected double verticalBufferSpace = 80;

    /*stores the two points that the screen is between*/
    public Vector2i screenPos1 = null;
    public Vector2i screenPos2 = null;

    /*literally just the aspect ratio*/
    protected double aspectRatio;

    /*size of the graphics*/
    Vector2i size;

    /*stores the last recorded size of the window*/
    Vector2i lastWindowSize;

    private ScreenTexture screenTexture;
    private SectoredGraphics uploadedGraphics;

    public RGBGraphicsScreen(RGBScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        Vector2i size = handler.getGraphics().size();
        aspectRatio = (double) size.x / size.y;
        this.size = handler.getGraphics().size();
        lastWindowSize = new Vector2i();
        adjustBounding();
    }

    public void adjustBounding(){
        Window window = MinecraftClient.getInstance().getWindow();

        if (lastWindowSize.equals(new Vector2i(window.getScaledWidth(), window.getScaledHeight()))) return;

        double bufferingSpaceX = (100 - horizontalBufferSpace) / 200;
        double bufferingSpaceY = (100 - verticalBufferSpace) / 200;

        double startX = window.getScaledWidth() * bufferingSpaceX;
        double startY = window.getScaledHeight() * bufferingSpaceY;
        double endX = window.getScaledWidth() * (horizontalBufferSpace / 100) + startX;
        double endY = window.getScaledHeight() * (verticalBufferSpace / 100) + startY;

        double targetRatio = aspectRatio;

        double boxWidth = endX - startX;
        double boxHeight = endY - startY;
        double boxRatio = boxWidth / boxHeight;

        double fittedWidth, fittedHeight;
        double offsetX = 0, offsetY = 0;

        if (boxRatio > targetRatio) {
            fittedHeight = boxHeight;
            fittedWidth = fittedHeight * targetRatio;
            offsetX = (boxWidth - fittedWidth) / 2.0;
        } else {
            fittedWidth = boxWidth;
            fittedHeight = fittedWidth / targetRatio;
            offsetY = (boxHeight - fittedHeight) / 2.0;
        }

        screenPos1 = new Vector2i((int)Math.round(startX + offsetX), (int)Math.round(startY + offsetY));
        screenPos2 = new Vector2i((int)Math.round(startX + offsetX + fittedWidth), (int)Math.round(startY + offsetY + fittedHeight));

        this.x = screenPos1.x - 4;
        this.y = screenPos1.y - 4;
        this.backgroundWidth = screenPos2.x - screenPos1.x + 8;
        this.backgroundHeight = screenPos2.y - screenPos1.y + 8;
        lastWindowSize = new Vector2i(window.getScaledWidth(), window.getScaledHeight());
    }

    public Vector2d graphicsToScreen(int x, int y) {
        double screenWidth = screenPos2.x - screenPos1.x;
        double screenHeight = screenPos2.y - screenPos1.y;

        SectoredGraphics graphics = handler.getGraphics();
        int logicalWidth = graphics.size().x;
        int logicalHeight = graphics.size().y;

        double scaleX = screenWidth / logicalWidth;
        double scaleY = screenHeight / logicalHeight;

        double screenX = screenPos1.x + x * scaleX;
        double screenY = screenPos1.y + y * scaleY;

        return new Vector2d(screenX, screenY);
    }

    @Override
    public Vector2i screenToGraphics(double screenX, double screenY) {
        if (screenX < screenPos1.x || screenX > screenPos2.x || screenY < screenPos1.y || screenY > screenPos2.y) return null;
        double screenWidth = screenPos2.x - screenPos1.x;
        double screenHeight = screenPos2.y - screenPos1.y;

        SectoredGraphics graphics = handler.getGraphics();
        int logicalWidth = graphics.size().x;
        int logicalHeight = graphics.size().y;

        double scaleX = screenWidth / logicalWidth;
        double scaleY = screenHeight / logicalHeight;

        int x = (int) ((screenX - screenPos1.x) / scaleX);
        int y = (int) ((screenY - screenPos1.y) / scaleY);

        return new Vector2i(x, y);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        onTick(mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
        context.fill(
                screenPos1.x - 4,
                screenPos1.y - 4,
                screenPos2.x + 4,
                screenPos2.y + 4,
                0xFFc6c6c6);
        context.fill(
                screenPos1.x - 2,
                screenPos1.y - 2,
                screenPos2.x + 2,
                screenPos2.y + 2,
                0xFFFFFFFF);
        context.draw();
        drawBackground(context,delta,mouseX,mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        adjustBounding();
        renderBackground(context, mouseX, mouseY, delta);

        SectoredGraphics graphics = handler.getGraphics();
        if (graphics == null) return;

        if (graphics != uploadedGraphics) {
            if (screenTexture == null) screenTexture = new ScreenTexture("gui");
            if (!screenTexture.upload(graphics)) return;
            uploadedGraphics = graphics;
        }

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        var program = ScreenShaders.getDisplayProgram();
        RenderSystem.setShader(program != null ? () -> program : GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, screenTexture.getId());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        float x1 = screenPos1.x, y1 = screenPos1.y, x2 = screenPos2.x, y2 = screenPos2.y;
        buffer.vertex(matrix, x1, y1, 0).texture(0, 0).color(255, 255, 255, 255);
        buffer.vertex(matrix, x1, y2, 0).texture(0, 1).color(255, 255, 255, 255);
        buffer.vertex(matrix, x2, y2, 0).texture(1, 1).color(255, 255, 255, 255);
        buffer.vertex(matrix, x2, y1, 0).texture(1, 0).color(255, 255, 255, 255);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        context.draw();
    }

    @Override
    public void removed() {
        super.removed();
        if (screenTexture != null) {
            screenTexture.close();
            screenTexture = null;
        }
        uploadedGraphics = null;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta){}
}
