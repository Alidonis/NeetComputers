package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.value.Value;

/**
 * represents an N.E.E.T. computers error, not to be confused with {@link java.lang.Exception} which works differently
 * @see Value
 * @see List
 * @see Tuple
 * @see Table
 * @see Function
 * @see java.lang.Exception
 */
public class Exception {
    private String Message;
    public Exception(String message){
        Message = message;
    }

    public String toString(){
        return Message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Exception){
            return Message.equals(obj.toString());
        }
        return super.equals(obj);
    }

    public Value<Exception> asValue(){
        return Value.of(this);
    }
}