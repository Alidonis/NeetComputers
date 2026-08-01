package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.value.Value;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;
import java.util.function.BiConsumer;

/**
 * represents an N.E.E.T. computers table of keys and associated values
 * @see Value
 * @see List
 * @see Tuple
 * @see Function
 * @see Exception
 * @see Hashtable
 */
public class Table {
    private final Hashtable<Value, Value> table = new Hashtable<>();

    public void put(Value key, Value value){
        table.put(key, value);
    }
    public void put(String key, Value value){
        table.put(Value.of(key), value);
    }
    public void put(String key, String value){
        table.put(Value.of(key), Value.of(value));
    }
    @Nullable
    public Value get(Value key){
        return table.get(key);
    }
    public Value get(String key){
        if (!table.containsKey(Value.of(key))) return Value.NULL;
        return table.get(Value.of(key));
    }
    public boolean contains(Value key){
        return table.containsKey(key);
    }
    public boolean contains(String key){
        return table.containsKey(Value.of(key));
    }
    public boolean isEmpty(){
        return table.isEmpty();
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
}