package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.value.Value;

public class Null{
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Null){
            return true;
        }
        return super.equals(obj);
    }
    public Value<Null> asValue(){
        return Value.NULL;
    }
}