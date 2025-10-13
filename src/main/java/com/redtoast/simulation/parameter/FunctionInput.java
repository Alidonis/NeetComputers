package com.redtoast.simulation.parameter;

import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.List;
import org.spongepowered.asm.mixin.injection.invoke.arg.ArgumentIndexOutOfBoundsException;

import java.util.Arrays;
import java.util.LinkedList;

public class FunctionInput {
    private LinkedList<Value> values;
    private List packed;
    private boolean doPacked = false;
    public FunctionInput(LinkedList<Value> data){
        values = data;
    }
    public FunctionInput(LinkedList<Value> data, LinkedList<Value> packedData){
        values = data;
        packed = new List(packedData);
    }

    public static FunctionInput fromArray(Value<?>[] args) {
        LinkedList<Value> list = new LinkedList<>(Arrays.asList(args));
        return new FunctionInput(list);
    }

    public List getPacked(){
        return packed;
    }
    public boolean isPacked(){
        return doPacked;
    }

    public int getSize(){
        return values.size();
    }
    public Value get(int index){
        if (index>=values.size()){
            throw new ArgumentIndexOutOfBoundsException(index);
        }
        return values.get(index);
    }
    public Value[] toArray(){
        Value[] vals = new Value[values.size() + packed.size()];
        for (int i = 0; i < values.size(); i++){
            vals[i] = values.get(i);
        }
        for (int i = 0; i < packed.size(); i++){
            vals[i + values.size()] = packed.get(i);
        }
        return vals;
    }
}