package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.value.Value;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * represents an N.E.E.T. computers list, interchangeable with {@link Tuple}
 * @see Value
 * @see Tuple
 * @see Table
 * @see Function
 * @see Exception
 * @see java.util.LinkedList
 */
public class List {
    private LinkedList<Value> vals;
    public List(Value<?>[] values){
        vals = new LinkedList<>(Arrays.stream(values).toList());
    }
    public List(LinkedList<Value> values){
        vals = values;
    }
    public List(Object... values){
        vals = new LinkedList<>(Arrays.stream(Value.of(values).toList().toArray()).toList());
    }
    public List(){vals = new LinkedList<>();}

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
    public boolean isPacked(){return false;}
    public Value<List> asValue(){
        return Value.of(this);
    }
    public Value[] toArray(){
        return vals.toArray(new Value[]{});
    }
    public List toList(){
        if (isPacked()){
            return new List(toArray());
        }else{
            return this;
        }
    }
    public Tuple toTuple(){
        if (isPacked()){
            return (Tuple) this;
        }else{
            return new Tuple(toArray());
        }
    }
}