package com.redtoast.simulation.parameter;

import com.redtoast.simulation.base.CustomParameter;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.VarType;
import org.jetbrains.annotations.Nullable;
import com.redtoast.simulation.Runtime;

import java.util.Objects;

public class ParameterRule {
    private boolean mode;
    public VarType type;
    private CustomParameter func;
    private boolean showType = false;
    private Integer index = null;
    private Integer range = null;
    public String disName = null;
    private boolean doIndex = false;
    public ParameterRule(CustomParameter function, VarType filter){
        func = function;
        mode = false;
        type=filter;
    }
    public ParameterRule(VarType filter) {
        type = filter;
        mode = true;
    }

    public void setRange(int owner){
        this.range = owner;
    }

    public void giveIndexOffset(int index, boolean doIndex){
        this.index = index;
        this.doIndex = doIndex;
    }

    public int getIndex(@Nullable Runtime runtime){
        if (index==null){
            return 0;
        }
        if (runtime==null){
            return index;
        }else{
            return index + (Objects.requireNonNull(runtime.getThread()).getLang().equals("Lua 5.2") ? 1 : 0);
        }
    }

    private boolean evalRange(int num, @Nullable Runtime runtime){
        int num2 = num ;
        if (num2<getIndex(runtime) && doIndex) return false;
        if (range ==null) return true;
        return num2<getRange(runtime) + getIndex(runtime);
    }

    public int getRange(@Nullable Runtime runtime){
        return range ==null ? 0 : range;
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
    public boolean check(Value value, @Nullable Runtime runtime) throws ExposedError {
        if (!mode){
            if (!value.instanceOf(type)) {
                showType = true;
                return false;
            }
            return func.rule(value);
        }else{
            if (value.instanceOf(VarType.NUMBER) && type==VarType.NUMBER){
                if (doIndex && range == null){
                    if (!evalRange(Objects.requireNonNull(value.toInt()), runtime)) throw new ExposedError("Invalid index, must be "+getIndex(runtime)+" or greater");
                }else if (range !=null){
                    if (!evalRange(Objects.requireNonNull(value.toInt()), runtime)) throw new ExposedError("Invalid index between "+getIndex(runtime)+" and "+getRange(runtime));
                }
            }
            return value.instanceOf(type);
        }
    }
    public String toString(int i, @Nullable Runtime runtime){
        if (!mode){
            if (type==VarType.ANY) return func.getName();
            return type.toString().toLowerCase() + ':' + func.getName() + ' ' + argName(i);
        }
        if (range !=null){
            return type.toString().toLowerCase() + '[' + getIndex(runtime) + '-' + getRange(runtime) + ']' + ' ' + argName(i);
        }
        return type.toString().toLowerCase() + ' ' + argName(i);
    }

    public String argName(int i){
        if (disName==null){
            return "arg"+i;
        }
        return disName;
    }
}