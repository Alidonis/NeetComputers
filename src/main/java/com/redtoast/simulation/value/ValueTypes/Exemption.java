package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.value.Value;

public class Exemption{
    private String Message;
    public Exemption(String message){
        Message = message;
    }

    public String toString(){
        return Message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Exemption){
            return Message.equals(obj.toString());
        }
        return super.equals(obj);
    }

    public Value<Exemption> asValue(){
        return new Value<>(this);
    }
}