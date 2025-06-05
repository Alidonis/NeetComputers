package com.redtoast.simulation.base;

import com.redtoast.simulation.value.Value;

public abstract class CustomParameter {
    public abstract boolean rule(Value arg);
    public abstract String getName();
}