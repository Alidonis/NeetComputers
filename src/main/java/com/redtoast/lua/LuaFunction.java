package com.redtoast.lua;

import org.luaj.vm2.LuaValue;

public interface LuaFunction {
    LuaValue main(LuaValue[] args);
    Rules getRules();
}
