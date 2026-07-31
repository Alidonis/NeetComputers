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
    private final ParameterRules ruleset;
    private String name;
    private final boolean userGenerated;

    public Function(boolean userGenerated){
        ruleset = new ParameterRules();
        this.userGenerated = userGenerated;
    }
    public Function(boolean userGenerated, String Name){
        name = Name;
        ruleset = new ParameterRules();
        this.userGenerated = userGenerated;
    }
    public Function(boolean userGenerated, ParameterRules rules){
        ruleset = rules;
        this.userGenerated = userGenerated;
    }
    public Function(boolean userGenerated, String Name, ParameterRules rules){
        ruleset=rules;
        name=Name;
        this.userGenerated = userGenerated;
    }
    public abstract Value call(FunctionInput parameters);
    public Value invoke(FunctionInput parameters){
        return call(parameters);
    }
    public ParameterRules getRules(){
        return ruleset;
    }
    public void setName(String Name){name = Name;}
    public String getName(){return name;}
    public boolean isUserGenerated() {return userGenerated;}
    public Value<Function> asValue(){
        return Value.of(this);
    }
}