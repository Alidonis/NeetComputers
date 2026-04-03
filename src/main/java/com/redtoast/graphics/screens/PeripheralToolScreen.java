package com.redtoast.graphics.screens;

import com.redtoast.neet.Networking.SetPeripheralTagPayload;
import com.redtoast.neet.Networking.SubmitCommandPayload;
import com.redtoast.simulation.value.VarType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

public class PeripheralToolScreen extends HandledScreen<PeripheralToolScreenHandler> {
    private final PeripheralToolScreenHandler handler;
    private TextFieldWidget textInput = null;
    private TextFieldWidget functionInput = null;
    private ButtonWidget submitButton = null;

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

    public PeripheralToolScreen(PeripheralToolScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
        Vector2i size = new Vector2i(500, 750);
        aspectRatio = (double) size.x / size.y;
        this.size = size;
        lastWindowSize = new Vector2i();
        adjustBounding();
    }

    @Override
    public void init() {
        textInput = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 200, 50, Text.of(Language.getInstance().get("menu.neetcomputers.tag_field")));
        textInput.setMaxLength(32);
        textInput.setPlaceholder(Text.of(Language.getInstance().get("menu.neetcomputers.tag_field")));
        if (handler.getTag()!=null) textInput.setText(handler.getTag());
        textInput.setChangedListener((text) -> ClientPlayNetworking.send(new SetPeripheralTagPayload(text, handler.syncId)));
        addSelectableChild(textInput);
        functionInput = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 200, 50, Text.of(Language.getInstance().get("menu.neetcomputers.function_field_title")));
        functionInput.setMaxLength(32);
        addSelectableChild(functionInput);
        submitButton = ButtonWidget.builder(Text.of(Language.getInstance().get("menu.neetcomputers.button")), (button) -> {
            ClientPlayNetworking.send(new SubmitCommandPayload(functionInput.getText(), handler.syncId));
        }).build();
        submitButton.setDimensions(33, 15);
        addSelectableChild(submitButton);
    }

    public boolean inbounds(int mouseX, int mouseY, int x1, int y1, int x2, int y2){
        return mouseY>=screenPos1.y+y1 && mouseY<=screenPos1.y+y2 && mouseX >= screenPos1.x+x1 && mouseX <= screenPos2.x-x2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        adjustBounding();
        textInput.setDimensions(screenPos2.x - screenPos1.x - 6, 15);
        textInput.setPosition(screenPos1.x+3, screenPos1.y+53);
        functionInput.setDimensions(screenPos2.x - screenPos1.x - 41, 15);
        functionInput.setPosition(screenPos1.x+3, screenPos1.y+83);
        submitButton.setPosition(screenPos2.x-36, screenPos1.y+83);
        context.drawText(MinecraftClient.getInstance().textRenderer, Language.getInstance().get("item.neetcomputers.peripheral_tool"), screenPos1.x+3, screenPos1.y+3, 0xFFFFFFFF, true);
        context.drawText(MinecraftClient.getInstance().textRenderer, Language.getInstance().get("menu.neetcomputers.tag_field_title"), screenPos1.x+3, screenPos1.y+43, 0xFFa8a8a8, true);
        context.drawText(MinecraftClient.getInstance().textRenderer, Language.getInstance().get("menu.neetcomputers.function_field_title"), screenPos1.x+3, screenPos1.y+73, 0xFFa8a8a8, true);
        context.drawText(MinecraftClient.getInstance().textRenderer, handler.getDisplayMessage(), screenPos1.x+3, screenPos1.y+100, handler.getVarType() == VarType.EXCEPTION ? 0xFFa80000 : 0xFFa8a8a8, true);
        boolean isUUID = inbounds(mouseX, mouseY, 3, 28, 3, 38);
        boolean isType = inbounds(mouseX, mouseY, 3, 18, 3, 27);
        boolean isRetur = inbounds(mouseX, mouseY, 3, 100, 3, 110) && handler.isLong();
        context.drawText(MinecraftClient.getInstance().textRenderer, Language.getInstance().get("menu.neetcomputers.uuid_field")+"...", screenPos1.x+3, screenPos1.y+28, isUUID ? 0xFFcccccc : 0xFFa8a8a8, true);
        context.drawText(MinecraftClient.getInstance().textRenderer, Language.getInstance().get("menu.neetcomputers.type_field")+"...", screenPos1.x+3, screenPos1.y+18, isType ? 0xFFcccccc : 0xFFa8a8a8, true);
        super.render(context, mouseX, mouseY, delta);
        context.fill(
                screenPos1.x - 2,
                screenPos1.y - 2,
                screenPos2.x + 2,
                screenPos2.y + 2,
                0xFF494949);
        context.fill(
                screenPos1.x,
                screenPos1.y,
                screenPos2.x,
                screenPos2.y,
                0xFF5b5b5b);
        context.draw();
        drawBackground(context,delta,mouseX,mouseY);
        textInput.render(context, mouseX, mouseY, delta);
        functionInput.render(context, mouseX, mouseY, delta);
        submitButton.render(context, mouseX, mouseY, delta);
        if (isType) context.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.of(handler.getTypeName()), mouseX, mouseY);
        if (isUUID) context.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.of(handler.getUuid()), mouseX, mouseY);
        if (isRetur) context.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.of(handler.getMessage()), mouseX, mouseY);
    }

    @Override
    public void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {

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

    //make it so pressing e while writing a tag doesn't close the menu
    @Override
    public boolean keyPressed(int keycode, int scancode, int modifiers){
        if (GLFW.GLFW_KEY_ENTER==keycode && functionInput.isActive()) {
            ClientPlayNetworking.send(new SubmitCommandPayload(functionInput.getText(), handler.syncId));
            return true;
        }
        if (client.options.inventoryKey.matchesKey(keycode, scancode) && (textInput.isActive() || functionInput.isActive())) return true;
        return super.keyPressed(keycode, scancode, modifiers);
    }
}
