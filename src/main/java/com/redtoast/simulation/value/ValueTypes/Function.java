package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.parameter.ParameterRules;
import com.redtoast.simulation.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents an N.E.E.T. computers callable function, call implementation, parameter handling, and error handling not included
 * @see Value
 * @see List
 * @see Tuple
 * @see Table
 * @see Exception
 * @see java.lang.reflect.Method
 */
public abstract class Function{
    private ParameterRules ruleset;
    private String name;

    private static final Logger error = LoggerFactory.getLogger("Neetcomputer: java function");
    public static void logError(String e){
        error.warn(e);
    }

    public Function(){
        ruleset = new ParameterRules();
    }
    public Function(String Name){
        name = Name;
        ruleset = new ParameterRules();
    }
    public Function(ParameterRules rules){
        ruleset = rules;
    }
    public Function(String Name, ParameterRules rules){
        ruleset=rules;
        name=Name;
    }
    public abstract Value call(FunctionInput parameters);
    public ParameterRules getRules(){
        return ruleset;
    }
    public void setName(String Name){name = Name;}
    public String getName(){return name;}

    public Value<Function> asValue(){
        return Value.of(this);
    }
}