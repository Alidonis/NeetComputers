package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.value.Value;

import java.util.LinkedList;

public class Tuple extends List{
    public Tuple(Value<?>[] values) {
        super(values);
    }

    public Tuple(LinkedList<Value> values) {
        super(values);
    }
}
