package com.redtoast.simulation;

import com.redtoast.simulation.base.GlobalGeneric;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Table;

import java.util.Hashtable;
import java.util.UUID;

public class GlobalManager extends Table {
    private Hashtable<UUID, GlobalGeneric> subGlobals = new Hashtable<>();
    private int blockoutTimer = 0;

    public GlobalManager(){
        super();
    }

    public void register(GlobalGeneric subGlobal){
        if (subGlobals.containsKey(subGlobal.getUUID())) return;
        subGlobals.put(subGlobal.getUUID(), subGlobal);
        super.foreach(subGlobal::insert);
    }

    @Override
    public void put(Value key, Value value){
        super.put(key, value);
        if (blockoutTimer>0){
            blockoutTimer--;
            return;
        }
        subGlobals.forEach((uuid, subGlobal) -> subGlobal.insert(key, value));
    }

    @Override
    public void put(String key, Value value){
        put(new Value<>(key), value);
    }

    @Override
    public void put(String key, String value){
        put(new Value<>(key), new Value<>(value));
    }

    @Override
    public void remove(Value key){
        super.remove(key);
        subGlobals.forEach((uuid, subGlobal) -> subGlobal.insert(key, Value.NULL));
    }

    public void remove(GlobalGeneric subGlobal){
        subGlobals.remove(subGlobal.getUUID());
    }

    public void put(UUID uuid, Value key, Value value){
        blockoutTimer++;
        super.put(key, value);
        subGlobals.forEach((uuid2, globalGeneric) -> {
            if (!uuid2.equals(uuid)) {
                globalGeneric.insert(key, value);
            }
        });
    }

    public GlobalGeneric[] getSubGlobals(){
        return subGlobals.values().toArray(new GlobalGeneric[0]);
    }
}