package com.redtoast.simulation.peripheral;

import com.redtoast.simulation.LangAPI.Value;
import com.redtoast.simulation.LangAPI.ValueTypes.Table;
import org.luaj.vm2.LuaTable;

import java.util.UUID;

public class peripheralWrapper {
    public UUID uuid;
    public String peripheralType;
    public Value<Table> table;
    public peripheralWrapper(peripheralAPI api){
        uuid = UUID.randomUUID();
        peripheralType = api.getLabel();
        table = api.getTable().asValue();
        Table metadata = new Table();
        metadata.put("tag","peripheral");
        metadata.put("uuid", uuid.toString());
        metadata.put("type", peripheralType);
        new Value(table).setMetaTable(metadata);
    }
}
