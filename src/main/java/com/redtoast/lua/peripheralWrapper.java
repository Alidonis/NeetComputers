package com.redtoast.lua;

import org.luaj.vm2.LuaTable;

import java.util.UUID;

public class peripheralWrapper {
    public UUID uuid;
    public String peripheralName;
    public LuaTable table;
    public peripheralWrapper(peripheralAPI api){
        uuid = UUID.randomUUID();
        peripheralName = api.getName();
        table = api.getTable();
    }
}
