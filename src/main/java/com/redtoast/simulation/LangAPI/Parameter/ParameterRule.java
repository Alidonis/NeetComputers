package com.redtoast.simulation.LangAPI.Parameter;

import com.redtoast.simulation.LangAPI.CustomParameter;
import com.redtoast.simulation.LangAPI.Value;
import com.redtoast.simulation.LangAPI.VarType;

public class ParameterRule {
    private boolean mode;
    public VarType type;
    private CustomParameter func;
    public ParameterRule(CustomParameter function){
        func = function;
        mode = false;
    }
    public ParameterRule(VarType filter) {
        type = filter;
        mode = true;
    }

    public String getName(){
        if (!mode){
            return func.getName();
        }else{
            return Value.VarName(type);
        }
    }
    public boolean check(Value value){
        if (!mode){
            return func.rule(value);
        }else{
            return value.instanceOf(type);
        }
    }
}