package com.redtoast.simulation.LangAPI.Parameter;

import com.redtoast.simulation.LangAPI.CustomParameter;
import com.redtoast.simulation.LangAPI.Value;
import com.redtoast.simulation.LangAPI.VarType;

public class ParameterRule {
    private boolean mode;
    public VarType type;
    private CustomParameter func;
    private boolean showType = false;
    public ParameterRule(CustomParameter function, VarType filter){
        func = function;
        mode = false;
        type=filter;
    }
    public ParameterRule(VarType filter) {
        type = filter;
        mode = true;
    }

    public String getName(){
        if (!mode){
            if (showType){
                showType=false;
                return Value.VarName(type);
            }
            return func.getName();
        }else{
            return Value.VarName(type);
        }
    }
    public boolean check(Value value){
        if (!mode){
            if (!value.instanceOf(type)) {
                showType = true;
                return false;
            }
            return func.rule(value);
        }else{
            return value.instanceOf(type);
        }
    }
}