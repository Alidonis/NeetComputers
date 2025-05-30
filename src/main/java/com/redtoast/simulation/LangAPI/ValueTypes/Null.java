package com.redtoast.simulation.LangAPI.ValueTypes;

import com.redtoast.simulation.LangAPI.Value;

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