package com.redtoast.lua.events;

import org.luaj.vm2.LuaValue;

public interface LuaEvent {
    String getName();
    LuaValue getValue();
}
