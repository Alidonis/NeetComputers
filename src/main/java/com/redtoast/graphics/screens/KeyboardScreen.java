package com.redtoast.graphics.screens;

import com.redtoast.simulation.events.EventGeneric;
import com.redtoast.simulation.value.Value;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public class KeyboardScreen extends HandledScreen<KeyboardScreenHandler> {
    public KeyboardScreen(KeyboardScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        drawForeground(context, mouseX, mouseY);
        context.drawText(MinecraftClient.getInstance().textRenderer, "dummy", 5, 5, 0xFFFFFFFF, true);
        context.draw();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.fill(0,0,Integer.MAX_VALUE,Integer.MAX_VALUE, 0x00FFFFFF);
        context.draw();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.fill(0,0,Integer.MAX_VALUE,Integer.MAX_VALUE, 0x00FFFFFF);
        context.draw();
    }

    @Override
    public boolean keyPressed(int keycode, int scancode, int modifiers){
        int code = BoilerplateScreen.mapGlfwKeyToAsciiCode(keycode, modifiers);
        if (code!=0){
            EventGeneric event = new EventGeneric("keyPressed",
                    Value.of(code),
                    Value.of(code>31 && code<128 ? (char) code : ""),
                    Value.of(modifiers)
            );
            event.send(handler);
        }
        if (client.options.inventoryKey.matchesKey(keycode, scancode)) return true;
        return super.keyPressed(keycode, scancode, modifiers);
    }

    @Override
    public boolean keyReleased(int keycode, int scancode, int modifiers){
        int code = BoilerplateScreen.mapGlfwKeyToAsciiCode(keycode, modifiers);
        if (code!=0){
            EventGeneric event = new EventGeneric("keyReleased",
                    Value.of(code),
                    Value.of(code>31 && code<128 ? (char) code : ""),
                    Value.of(modifiers)
            );
            event.send(handler);
        }
        return super.keyReleased(keycode, scancode, modifiers);
    }
}
