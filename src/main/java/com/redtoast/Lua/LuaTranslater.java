package com.redtoast.Lua;

import com.redtoast.simulation.base.LanguageTranslater;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.parameter.ParameterCheckReturn;
import com.redtoast.simulation.parameter.ParameterRules;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.*;
import com.redtoast.simulation.value.VarType;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;

public class LuaTranslater implements LanguageTranslater<Varargs, Varargs> {
    private abstract static class FunctionWrapper extends VarArgFunction {
        @Override
        public abstract Varargs invoke(Varargs args);
    }

    @Override
    public Varargs fromValue(Value var){
        Varargs data = fromValueWithoutMetadata(var);
        if (var.hasMetadata() && !var.instanceOf(VarType.TUPLE)){
            assert data instanceof LuaValue;
            Varargs val = fromValueWithoutMetadata(var.getMetaTable().asValue());
            ((LuaValue) data).setmetatable((LuaValue) val);
        }
        return data;
    }

    public Varargs fromValueWithoutMetadata(Value var) {
        switch (var.getType()){
            case INT:
                assert var.getValue() instanceof Integer;
                return LuaValue.valueOf((int) var.getValue());
            case DOUBLE:
                assert var.getValue() instanceof Double;
                return LuaValue.valueOf((double) var.getValue());
            case FLOAT:
                assert var.getValue() instanceof Float;
                return LuaValue.valueOf((float) var.getValue());
            case BOOLEAN:
                assert var.getValue() instanceof Boolean;
                return LuaValue.valueOf((boolean) var.getValue());
            case STRING:
                assert var.getValue() instanceof String;
                return LuaValue.valueOf((String) var.getValue());
            case TABLE:
                assert var.getValue() instanceof Table;
                Table table = (Table) var.getValue();
                LuaTable newTable = new LuaTable();
                table.foreach((key, value) -> {
                    Varargs rawKey = fromValue(key.serialize());
                    assert rawKey instanceof LuaValue;
                    Varargs rawValue = fromValue(value.serialize());
                    assert rawValue instanceof LuaValue;
                    newTable.set((LuaValue) rawKey, (LuaValue) rawValue);
                });
                return newTable;
            case LIST, TUPLE:
                assert var.getValue() instanceof List;
                int size = ((List) var.getValue()).size();
                LuaValue[] values = new LuaValue[size];
                for (int i = 0; i < size; i++){
                    Varargs rawValue = fromValue(((List) var.serialize().getValue()).get(i));
                    values[i] = (LuaValue) rawValue;
                }
                LuaTable array = LuaValue.listOf(values);
                if (var.getValue() instanceof Tuple){
                    return array.unpack();
                }else{
                    return array;
                }
            case FUNCTION:
                assert var.getValue() instanceof Function;
                return new FunctionWrapper() {
                    @Override
                    public Varargs invoke(Varargs args) {
                        Value[] values = new Value[args.narg()];
                        for (int i = 0; i < args.narg(); i++){
                            values[i] = toValue(args.arg(i+1));
                        }
                        ParameterRules rules = ((Function) var.getValue()).getRules();
                        ParameterCheckReturn check = ParameterRules.checkParameters(values, rules);
                        if (check.isError()){
                            String message = check.getMessage()
                                .replaceAll("null", "nil")
                                .replaceAll("int", "number")
                                .replaceAll("double", "number")
                                .replaceAll("float", "number");
                            return LuaValue.error(message);
                        }else{
                            Value output = ((Function) var.getValue()).call(check.getFunctionInput());
                            return fromValue(output);
                        }
                    }
                };
            case EXEMPTION:
                assert var.getValue() instanceof Exemption;
                return LuaValue.error(var.getValue().toString());
            case NULL:
                return LuaValue.NIL;
        }
        return LuaValue.NIL;
    }

    @Override
    public Value<?> toValue(Varargs var){
        Value<?> data = toValueWithoutMetadata(var);
        if (var instanceof LuaValue val){
            data.setMetaTable(toValueWithoutMetadata(val).toTable());
        }
        return data;
    }

    public Value<?> toValueWithoutMetadata(Varargs var) {
        if (!(var instanceof LuaValue)){
            Value[] values = new Value<?>[var.narg()];
            for (int i = 0; i < var.narg(); i++){
                values[i] = toValue(var.arg(i+1));
            }
            return new Value<>(new Tuple(values));
        }
        assert var instanceof LuaValue;
        LuaValue val = (LuaValue) var;
        if (val instanceof LuaInteger){
            return new Value<>(val.toint());
        }else if (val instanceof LuaDouble){
            return new Value<>(val.todouble());
        }else if (val instanceof LuaBoolean){
            return new Value<>(val.toboolean());
        }else if (val instanceof LuaString){
            return new Value<>(val.toString());
        }else if (val instanceof LuaTable table){
            boolean isArray = false;
            Value value = Value.NULL;
            LuaValue k = LuaValue. NIL;
            while ( true ) {
                Varargs n = table. next(k);
                if ( (k = n. arg1()).isnil() )
                    break;
                LuaValue v = n. arg(2);
                if (value.isNull()){
                    isArray = v.isnil();
                    if (isArray){
                        value = new Value<>(new List(new Value[]{toValue(k)}));
                    }else{
                        value = new Value<>(new Table());
                        assert value.getValue() instanceof Table;
                        ((Table) value.getValue()).put(toValue(k), toValue(v));
                    }
                }else{
                    if (isArray){
                        assert value.getValue() instanceof List;
                        ((List) value.getValue()).add(toValue(k));
                    }else{
                        assert value.getValue() instanceof Table;
                        ((Table) value.getValue()).put(toValue(k), toValue(v));
                    }
                }
            }
            return value;
        }else if (val instanceof LuaFunction function){
            return new Value<>(new Function(ParameterRules.ANY) {
                @Override
                public Value call(FunctionInput parameters) {
                    try{
                        LuaValue[] values = new LuaValue[parameters.getSize()];
                        for (int i = 0; i < parameters.getSize(); i++){
                            Varargs rawValue = fromValue(parameters.get(i).serialize());
                            values[i] = (LuaValue) rawValue;
                        }
                        Varargs output = function.invoke(values);
                        return toValue(output);
                    }catch (Exception e){
                        return new Value(new Exemption(e.getMessage()));
                    }
                }
            });
        }
        return Value.NULL;
    }
}
