package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.parameter.ParameterRules;
import com.redtoast.simulation.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

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
    private boolean mainThread = false;
    private final Runtime runtime;

    private static final Logger error = LoggerFactory.getLogger("Neetcomputer: java function");
    public static void logError(String e){
        error.warn(e);
    }

    public Function(Runtime runtime){
        ruleset = new ParameterRules();
        this.runtime = runtime;
    }
    public Function(Runtime runtime, String Name){
        name = Name;
        ruleset = new ParameterRules();
        this.runtime = runtime;
    }
    public Function(Runtime runtime, ParameterRules rules){
        ruleset = rules;
        this.runtime = runtime;
    }
    public Function(Runtime runtime, String Name, ParameterRules rules){
        ruleset=rules;
        name=Name;
        this.runtime = runtime;
    }
    public void makeMainThread(){
        mainThread = true;
    }
    public abstract Value call(FunctionInput parameters);
    public Value invoke(FunctionInput parameters){
        if (mainThread && runtime!=null) {
            runtime.queCall(this::call, parameters);
            if (runtime.pullQue().isEmpty()) Objects.requireNonNull(runtime.getThread()).yield();
            return runtime.pullQue().get();
        }
        return call(parameters);
    }
    public ParameterRules getRules(){
        return ruleset;
    }
    public void setName(String Name){name = Name;}
    public String getName(){return name;}

    public Value<Function> asValue(){
        return Value.of(this);
    }
}