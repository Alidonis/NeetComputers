package com.redtoast.graphics.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.redtoast.graphics.RGBGraphicsArray;
import dan200.computercraft.client.gui.ComputerScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.mojang.blaze3d.systems.RenderSystem.*;

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

    public RGBGraphicsScreen(RGBScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        Vector2i size = handler.getGraphics().getSize();
        aspectRatio = (double) size.x / size.y;
        this.size = handler.getGraphics().getSize();
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

        RGBGraphicsArray graphics = handler.getGraphics();
        int logicalWidth = graphics.getSize().x;
        int logicalHeight = graphics.getSize().y;

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

        RGBGraphicsArray graphics = handler.getGraphics();
        int logicalWidth = graphics.getSize().x;
        int logicalHeight = graphics.getSize().y;

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

        Canvas canvas = Canvas.getCanvas(context);
        RGBGraphicsArray graphics = handler.getGraphics();
        Vector2i size = graphics.getSize();
        int width = size.x;
        int height = size.y;

        for (int y = 0; y < height; y++) {
            Integer lastColor = null;
            Vector2d startPos = null;
            Vector2d endPos;

            for (int x = 0; x < width; x++) {
                int currentColor = graphics.get(x, y);

                if (lastColor == null) {
                    lastColor = currentColor;
                    startPos = graphicsToScreen(x, y);
                }

                boolean isLastColumn = (x == width - 1);
                boolean colorChanged = currentColor != lastColor;

                if (colorChanged || isLastColumn) {
                    endPos = graphicsToScreen(x + (isLastColumn && !colorChanged ? 1 : 0), y + 1);
                    canvas.placeRec(startPos.x, startPos.y, endPos.x, endPos.y, lastColor);

                    if (!isLastColumn) {
                        startPos = graphicsToScreen(x, y);
                        lastColor = currentColor;
                    }
                }
            }
        }
        canvas.draw();
    }

    public record Canvas(DrawContext context, Matrix4f matrix, Tessellator tessellator, BufferBuilder buffer){
        public static Canvas getCanvas(DrawContext context){
            Matrix4f transformationMatrix = context.getMatrices().peek().getPositionMatrix();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            return new Canvas(context, transformationMatrix, tessellator, buffer);
        }

        public void placeRec(double x1, double y1, double x2, double y2, int color){
            color = RGBGraphicsArray.blendPixel(0xFF000000, color | 0xFF000000);

            buffer.vertex(matrix, (float) x2, (float) y2, 0).color(color);
            buffer.vertex(matrix, (float) x2, (float) y1, 0).color(color);
            buffer.vertex(matrix, (float) x1, (float) y1, 0).color(color);

            buffer.vertex(matrix, (float) x1, (float) y1, 0).color(color);
            buffer.vertex(matrix, (float) x1, (float) y2, 0).color(color);
            buffer.vertex(matrix, (float) x2, (float) y2, 0).color(color);
        }

        public void draw(){
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            context.draw();
        }
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta){}
}