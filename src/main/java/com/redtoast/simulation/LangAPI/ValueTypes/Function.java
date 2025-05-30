package com.redtoast.simulation.LangAPI.ValueTypes;

import com.redtoast.simulation.LangAPI.Parameter.FunctionInput;
import com.redtoast.simulation.LangAPI.Parameter.ParameterRules;
import com.redtoast.simulation.LangAPI.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Function{
    private ParameterRules ruleset;

    private static final Logger error = LoggerFactory.getLogger("Neetcomputer: java function");
    public static void logError(Exception e){
        error.warn(e.toString());
    }

    public Function(){
        ruleset = new ParameterRules();
    }
    public Function(ParameterRules rules){
        ruleset = rules;
    }
    public abstract Value call(FunctionInput parameters);
    public ParameterRules getRules(){
        return ruleset;
    }

    public Value<Function> asValue(){
        return new Value<>(this);
    }
}