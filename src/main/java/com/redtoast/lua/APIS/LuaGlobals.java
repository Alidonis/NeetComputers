package com.redtoast.lua.APIS;

import com.redtoast.lua.LuaAPI;
import com.redtoast.lua.LuaVM;

public class LuaGlobals extends LuaAPI {
    private LuaVM vm;
    public LuaGlobals(LuaVM VM) {
        super("");
        vm = VM;
    }
}
