package com.redtoast.simulation.LangAPI.ValueTypes;

import com.redtoast.simulation.LangAPI.Value;

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