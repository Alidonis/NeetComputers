package com.redtoast.simulation.LangAPI.ValueTypes;

import com.redtoast.simulation.LangAPI.Value;

import java.util.LinkedList;

public class Tuple extends List{
    public Tuple(Value<?>[] values) {
        super(values);
    }

    public Tuple(LinkedList<Value> values) {
        super(values);
    }
}
