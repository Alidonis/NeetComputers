package com.redtoast.graphics.screens;

import com.redtoast.simulation.EventGeneric;
import com.redtoast.simulation.value.Value;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.Hashtable;

/**
 * Abstract class designed to hold most of the boilerplate and I/O functions of {@link RGBGraphicsScreen}
 * <p>not supposed to be used on its own</p>
 */
public abstract class BoilerplateScreen extends HandledScreen<RGBScreenHandler> {
    private Hashtable<Integer, Vector2i> dragTable = new Hashtable<>();
    private Hashtable<Integer, Boolean> repeatTable = new Hashtable<>();

    public RGBScreenHandler handler;

    public BoilerplateScreen(RGBScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
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
    public abstract Vector2i screenToGraphics(double screenX, double screenY);

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int key){
        Vector2i pos = screenToGraphics(mouseX, mouseY);
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
        Vector2i pos = screenToGraphics(mouseX, mouseY);
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
        Vector2i pos = screenToGraphics(mouseX, mouseY);
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
        Vector2i pos = screenToGraphics(mouseX, mouseY);
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
}
