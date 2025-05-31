package com.redtoast.simulation.LangAPI.Parameter;

import com.redtoast.simulation.LangAPI.Value;
import com.redtoast.simulation.LangAPI.VarType;

public class ParameterRule {
    private boolean mode;
    public VarType type;
    private LambdaRule func;
    private boolean nullable = false;
    public ParameterRule(LambdaRule function){
        func = function;
        mode = false;
    }
    public ParameterRule(LambdaRule function, boolean optional){
        func = function;
        nullable = optional;
        mode = false;
    }
    public ParameterRule(VarType filter){
        type = filter;
        mode = true;
    }
    public ParameterRule(VarType filter, boolean optional){
        type = filter;
        nullable = optional;
        mode = true;
    }

    public boolean isOptional(){
        return nullable;
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