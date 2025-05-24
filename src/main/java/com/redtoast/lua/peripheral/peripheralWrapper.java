package com.redtoast.lua.peripheral;

import org.luaj.vm2.LuaTable;

import java.util.UUID;

public class peripheralWrapper {
    public UUID uuid;
    public String peripheralType;
    public LuaTable table;
    public peripheralWrapper(peripheralAPI api){
        uuid = UUID.randomUUID();
        peripheralType = api.getName();
        table = api.getTable();
        LuaTable metadata = new LuaTable();
        metadata.set("tag","peripheral");
        metadata.set("uuid", uuid.toString());
        metadata.set("type", peripheralType);
        table.setmetatable(metadata);
    }
}
