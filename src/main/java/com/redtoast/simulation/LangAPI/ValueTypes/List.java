package com.redtoast.simulation.LangAPI.ValueTypes;

import com.redtoast.simulation.LangAPI.Value;

import java.util.Arrays;
import java.util.LinkedList;

public class List {
    private LinkedList<Value> vals;
    public List(Value<?>[] values){
        vals = new LinkedList<>(Arrays.stream(values).toList());
    }
    public List(LinkedList<Value> values){
        vals = values;
    }

    public void add(Value value){
        vals.add(value);
    }
    public Value get(int index){
        return vals.get(index);
    }
    public void set(int index, Value value){
        vals.set(index, value);
    }
    public int size(){
        return vals.size();
    }

    public Value<List> asValue(){
        return new Value<>(this);
    }
}