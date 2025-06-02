package com.redtoast.simulation.LangAPI.Parameter;

import com.redtoast.simulation.LangAPI.CustomParameter;
import com.redtoast.simulation.LangAPI.Value;
import com.redtoast.simulation.LangAPI.VarType;

import java.util.Arrays;
import java.util.LinkedList;

public class ParameterRules {
    private final LinkedList<ParameterRule> rules = new LinkedList<>();
    public boolean packExtra = false;
    public ParameterRule packRule;

    public final static ParameterRules ANY = new ParameterRules(){
        @Override
        public boolean doAny(){
            return true;
        }
    };
    public final static ParameterRules NONE = new ParameterRules();

    public boolean doAny(){return false;}
    public ParameterRules(){}
    public ParameterRules(VarType rule){
        rules.add(new ParameterRule(rule));
    }
    public ParameterRules(CustomParameter rule){
        rules.add(new ParameterRule(rule));
    }

    public ParameterRules add(VarType rule){
        rules.add(new ParameterRule(rule));
        return this;
    }
    public ParameterRules add(CustomParameter rule){
        rules.add(new ParameterRule(rule));
        return this;
    }
    public ParameterRules allowPacking(VarType rule){
        packExtra = true;
        packRule = new ParameterRule(rule);
        return this;
    }
    public ParameterRules allowPacking(CustomParameter rule){
        packExtra = true;
        packRule = new ParameterRule(rule);
        return this;
    }

    public static ParameterCheckReturn checkParameters(Value[] values, ParameterRules ruleset){
        if (ruleset.doAny()){
            return new ParameterCheckReturn(new FunctionInput(new LinkedList<>(Arrays.asList(values))));
        }
        LinkedList<Value> output = new LinkedList<>();
        LinkedList<Value> packed = new LinkedList<>();
        for (int i = 0; i < Math.max(values.length, ruleset.rules.size()); i++){
            if (i < ruleset.rules.size()){
                if (i < values.length){
                    if (ruleset.rules.get(i).check(values[i])){
                        output.add(values[i]);
                    }else{
                        return new ParameterCheckReturn("Argument #"+i+": Expected "+ruleset.rules.get(i).getName()+", got "+values[i].typeName());
                    }
                }else{
                    return new ParameterCheckReturn("Argument #"+i+": Expected "+ruleset.rules.get(i).getName()+", got null");
                }
            }else{
                if (ruleset.packExtra){
                    packed.add(values[i]);
                }else{
                    return new ParameterCheckReturn("Argument #"+i+": Expected null, got "+values[i].typeName());
                }
            }
        }
        if (ruleset.packExtra){
            return new ParameterCheckReturn(new FunctionInput(output, packed));
        }else{
            return new ParameterCheckReturn(new FunctionInput(output));
        }
    }
}