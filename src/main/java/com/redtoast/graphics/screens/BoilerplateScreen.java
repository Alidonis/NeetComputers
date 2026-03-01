package com.redtoast.graphics.screens;

import com.redtoast.simulation.events.EventGeneric;
import com.redtoast.simulation.value.Value;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import java.util.Hashtable;

import static org.lwjgl.glfw.GLFW.*;

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
        int code = mapGlfwKeyToAsciiCode(keycode, modifiers);
        if (code!=0){
            EventGeneric event = new EventGeneric("keyPressed",
                    Value.of(code),
                    Value.of(code>31 && code<128 ? (char) code : Value.NULL),
                    Value.of(modifiers)
            );
            event.send(handler);
        }
        if (client.options.inventoryKey.matchesKey(keycode, scancode)) return true;
        return super.keyPressed(keycode, scancode, modifiers);
    }
    @Override
    public boolean keyReleased(int keycode, int scancode, int modifiers){
        int code = mapGlfwKeyToAsciiCode(keycode, modifiers);
        if (code!=0){
            EventGeneric event = new EventGeneric("keyReleased",
                    Value.of(code),
                    Value.of(code>31 && code<128 ? (char) code : Value.NULL),
                    Value.of(modifiers)
            );
            event.send(handler);
        }
        return super.keyReleased(keycode, scancode, modifiers);
    }
    public abstract Vector2i screenToGraphics(double screenX, double screenY);
    public void onTick(int mouseX, int mouseY){
        Vector2i pos = screenToGraphics(mouseX, mouseY);
        if (pos!=null){
            if (!dragTable.containsKey(-1) || !dragTable.get(-1).equals(pos)){
                EventGeneric event = new EventGeneric("mouseMoved",
                        Value.of(pos.x),
                        Value.of(pos.y)
                );
                event.send(handler);
            }
            dragTable.put(-1, pos);
        }
    }
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount){
        Vector2i pos = screenToGraphics(mouseX, mouseY);
        if (pos!=null){
            EventGeneric event = new EventGeneric("mouseScrolled",
                    Value.of(pos.x),
                    Value.of(pos.y),
                    Value.of(horizontalAmount),
                    Value.of(verticalAmount)
            );
            event.send(handler);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
    public static int mapGlfwKeyToAsciiCode(int key, int mods) {
        boolean shift = (mods & GLFW.GLFW_MOD_SHIFT) != 0;

        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            int base = 'a' + (key - GLFW.GLFW_KEY_A);
            return shift ? Character.toUpperCase(base) : base;
        }

        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
            if (shift) {
                return switch (key) {
                    case GLFW.GLFW_KEY_1 -> '!';
                    case GLFW.GLFW_KEY_2 -> '@';
                    case GLFW.GLFW_KEY_3 -> '#';
                    case GLFW.GLFW_KEY_4 -> '$';
                    case GLFW.GLFW_KEY_5 -> '%';
                    case GLFW.GLFW_KEY_6 -> '^';
                    case GLFW.GLFW_KEY_7 -> '&';
                    case GLFW.GLFW_KEY_8 -> '*';
                    case GLFW.GLFW_KEY_9 -> '(';
                    case GLFW.GLFW_KEY_0 -> ')';
                    default -> 0;
                };
            } else {
                return '0' + (key - GLFW.GLFW_KEY_0);
            }
        }

        if (key >= GLFW.GLFW_KEY_F1 && key <= GLFW_KEY_F25) {
            return 134 + (key - GLFW.GLFW_KEY_F1);
        }

        return switch (key) {
            case GLFW_KEY_SPACE -> ' ';
            case GLFW_KEY_APOSTROPHE -> shift ? '"' : '\'';
            case GLFW_KEY_COMMA -> shift ? '<' : ',';
            case GLFW_KEY_MINUS -> shift ? '_' : '-';
            case GLFW_KEY_PERIOD -> shift ? '>' : '.';
            case GLFW_KEY_SLASH -> shift ? '?' : '/';
            case GLFW_KEY_SEMICOLON -> shift ? ':' : ';';
            case GLFW_KEY_EQUAL -> shift ? '+' : '=';
            case GLFW_KEY_LEFT_BRACKET -> shift ? '{' : '[';
            case GLFW_KEY_BACKSLASH -> shift ? '|' : '\\';
            case GLFW_KEY_RIGHT_BRACKET -> shift ? '}' : ']';
            case GLFW_KEY_GRAVE_ACCENT -> shift ? '~' : '`';
            case GLFW_KEY_ENTER -> 13;
            case GLFW_KEY_BACKSPACE, GLFW_KEY_DELETE -> 8;
            case GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT -> 14;
            case GLFW_KEY_LEFT -> 128;
            case GLFW_KEY_RIGHT -> 129;
            case GLFW_KEY_UP -> 130;
            case GLFW_KEY_DOWN -> 131;
            case GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL -> 132;
            case GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT -> 133;
            default -> 0;
        };
    }
}
