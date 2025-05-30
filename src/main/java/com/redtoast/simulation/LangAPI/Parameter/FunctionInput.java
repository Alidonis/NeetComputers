package com.redtoast.simulation.LangAPI.Parameter;

import com.redtoast.simulation.LangAPI.Value;
import com.redtoast.simulation.LangAPI.ValueTypes.List;
import org.spongepowered.asm.mixin.injection.invoke.arg.ArgumentIndexOutOfBoundsException;

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

    public List getPacked(){
        return packed;
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
}