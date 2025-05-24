package com.redtoast.lua.events;

import org.joml.Vector2i;
import org.luaj.vm2.LuaValue;

public class MouseMoveEvent implements LuaEvent {
    Vector2i mousePos;

    public MouseMoveEvent(int MouseX, int MouseY) {
        mousePos = new Vector2i(MouseX,MouseY);
    }

    @Override
    public String getName() {
        return "mouse_pos_changed";
    }

    @Override
    public LuaValue getValue() {
        return LuaValue.userdataOf(mousePos);
    }
}
