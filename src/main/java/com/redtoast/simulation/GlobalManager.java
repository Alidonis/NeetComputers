package com.redtoast.simulation;

import com.redtoast.simulation.base.GlobalGeneric;
import com.redtoast.simulation.value.NVTable;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Table;

import java.util.Hashtable;
import java.util.UUID;

public class GlobalManager extends Table {
    private Hashtable<UUID, GlobalGeneric> subGlobals = new Hashtable<>();
    public NVTable NVRam;
    private int blockoutTimer = 0;
    private Runtime runtime;

    public GlobalManager(Runtime runtime, NVTable NVRam){
        super();
        this.runtime = runtime;
        this.NVRam = NVRam;
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
        put(Value.of(key), value);
    }

    @Override
    public void put(String key, String value){
        put(Value.of(key), Value.of(value));
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

    public Runtime getParent() {return runtime;}
}