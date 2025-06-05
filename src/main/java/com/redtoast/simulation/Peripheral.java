package com.redtoast.simulation;

import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Table;

import java.util.UUID;

public class Peripheral {
    public UUID uuid;
    public String peripheralType;
    public Value<Table> table;

    public Peripheral(String type, Table API){
        uuid = UUID.randomUUID();
        peripheralType = type;
        Table metadata = new Table();
        metadata.put("tag","peripheral");
        metadata.put("uuid", uuid.toString());
        metadata.put("type", peripheralType);
        Value<Table> SuperTable = API.asValue();
        SuperTable.setMetaTable(metadata);
        table = SuperTable;
    }
}
