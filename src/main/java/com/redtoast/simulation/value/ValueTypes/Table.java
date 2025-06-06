package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.value.Value;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;
import java.util.function.BiConsumer;

public class Table{
    private Hashtable<Value, Value> table = new Hashtable<>();
    public void put(Value key, Value value){
        table.put(key, value);
    }
    public void put(String key, Value value){
        table.put(new Value<>(key), value);
    }
    public void put(String key, String value){
        table.put(new Value<>(key), new Value<>(value));
    }
    @Nullable
    public Value get(Value key){
        return table.get(key);
    }
    @Nullable
    public Value get(String key){
        return table.get(new Value<>(key));
    }
    public boolean contains(Value key){
        return table.contains(key);
    }
    public boolean contains(String key){
        return table.contains(new Value<>(key));
    }
    public void foreach(BiConsumer<? super Value, ? super Value> action){
        table.forEach(action);
    }
    @Override
    public boolean equals(Object obj) {
        return table.equals(obj);
    }
    public void remove(Value key){
        table.remove(key);
    }

    public Value<Table> asValue(){
        return new Value<>(this);
    }
}