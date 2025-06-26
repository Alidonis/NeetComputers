package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.value.ComplexValue;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.VarType;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;
import java.util.Objects;
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
public class Table implements ComplexValue<Table> {
    private Hashtable<Value, Value> table = new Hashtable<>();
    private Table metadata = null;
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
    @Nullable
    public Value get(String key){
        return table.get(Value.of(key));
    }
    public boolean contains(Value key){
        return table.contains(key);
    }
    public boolean contains(String key){
        return table.contains(Value.of(key));
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

    @Override
    public Value<Table> asValue(){
        return Value.of(this);
    }

    @Override
    public void setMetaTable(Table metadata) {
        this.metadata = metadata;
    }

    @Override
    public @Nullable Table getMetaTable() {
        return metadata;
    }

    @Override
    public boolean hasMetaTable() {
        return metadata!=null;
    }

    @Override
    public void setMeta(Object key, Object value) {
        if (metadata==null) return;
        metadata.put(Value.of(key), Value.of(value));
    }

    @Override
    public Value getMeta(Object key) {
        if (metadata==null) return null;
        return metadata.get(Value.of(key));
    }
}