package com.redtoast.simulation.peripheral;

import com.redtoast.simulation.LangAPI.ValueTypes.Table;
import org.luaj.vm2.LuaTable;

import java.util.UUID;

public class peripheralWrapper {
    public UUID uuid;
    public String peripheralType;
    public Table table;
    public peripheralWrapper(peripheralAPI api){
        uuid = UUID.randomUUID();
        peripheralType = api.getLabel();
        table = api.getTable();
        LuaTable metadata = new LuaTable();
        metadata.set("tag","peripheral");
        metadata.set("uuid", uuid.toString());
        metadata.set("type", peripheralType);
        //table.setmetatable(metadata);
    }
}
