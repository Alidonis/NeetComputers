package com.redtoast.simulation.LangAPI;

import com.redtoast.simulation.LangAPI.ValueTypes.Function;

import java.util.LinkedList;

public abstract class CustomParameter {
    public abstract boolean rule(Value arg);
    public abstract String getName();
    public Class<? extends CustomParameter> format(){
        return this.getClass();
    }
}