package com.redtoast.simulation.parameter;

import com.redtoast.simulation.base.CustomParameter;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.VarType;
import org.jetbrains.annotations.Nullable;
import com.redtoast.simulation.Runtime;

import java.util.Arrays;
import java.util.LinkedList;

public class ParameterRules {
    public final LinkedList<ParameterRule> rules = new LinkedList<>();
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
    public ParameterRules(CustomParameter rule, VarType type){
        rules.add(new ParameterRule(rule, type));
    }

    public ParameterRules add(VarType rule){
        rules.add(new ParameterRule(rule));
        return this;
    }
    public ParameterRules add(CustomParameter rule, VarType type){
        rules.add(new ParameterRule(rule, type));
        return this;
    }
    public ParameterRules allowPacking(VarType rule){
        packExtra = true;
        packRule = new ParameterRule(rule);
        return this;
    }
    public ParameterRules allowPacking(CustomParameter rule, VarType type){
        packExtra = true;
        packRule = new ParameterRule(rule, type);
        return this;
    }

    public static ParameterCheckReturn checkParameters(Value[] values, ParameterRules ruleset, @Nullable Runtime runtime){
        if (ruleset.doAny()){
            return new ParameterCheckReturn(new FunctionInput(new LinkedList<>(Arrays.asList(values)), new LinkedList<>()));
        }
        if (values==null) values = new Value[0];
        LinkedList<Value> output = new LinkedList<>();
        LinkedList<Value> packed = new LinkedList<>();
        for (int i = 0; i < Math.max(values.length, ruleset.rules.size()); i++){
            if (i < ruleset.rules.size()){
                if (i < values.length){
                    try{
                        if (ruleset.rules.get(i).check(values[i], runtime)){
                            output.add(values[i]);
                        }else{
                            return new ParameterCheckReturn("Argument #"+i+": Expected "+ruleset.rules.get(i).getName()+", got "+values[i].typeName());
                        }
                    }catch (ExposedError err){
                        return new ParameterCheckReturn("Argument #"+i+": "+err.getMessage());
                    }
                }else{
                    return new ParameterCheckReturn("Argument #"+i+": Expected "+ruleset.rules.get(i).getName()+", got null");
                }
            }else{
                if (ruleset.packExtra){
                    try{
                        if (ruleset.rules.get(i).check(values[i], runtime)){
                            packed.add(values[i]);
                        }else{
                            return new ParameterCheckReturn("Argument #"+i+": Expected "+ruleset.rules.get(i).getName()+", got "+values[i].typeName());
                        }
                    }catch (ExposedError err){
                        return new ParameterCheckReturn("Argument #"+i+": "+err.getMessage());
                    }
                }else{
                    return new ParameterCheckReturn("Argument #"+i+": Expected null, got "+values[i].typeName());
                }
            }
        }
        if (ruleset.packExtra){
            return new ParameterCheckReturn(new FunctionInput(output, packed));
        }else{
            return new ParameterCheckReturn(new FunctionInput(output, new LinkedList<>()));
        }
    }

    public String toString(@Nullable Runtime runtime){
        StringBuilder buffer = new StringBuilder();
        buffer.append('(');
        for (int i = 0; i < rules.size(); i++){
            buffer.append(rules.get(i).toString(i, runtime));
            if (i < rules.size() - 1) buffer.append(", ");
        }
        if (packExtra){
            buffer.append(", ");
            buffer.append(packRule.toString(rules.size(), runtime));
        }
        buffer.append(')');
        return buffer.toString();
    }
}