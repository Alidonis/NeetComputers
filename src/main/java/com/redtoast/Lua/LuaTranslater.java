package com.redtoast.Lua;

import com.redtoast.simulation.base.LanguageTranslater;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.parameter.ParameterCheckReturn;
import com.redtoast.simulation.parameter.ParameterRules;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.*;
import com.redtoast.simulation.value.ValueTypes.Exception;
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
        if (var==null) return LuaValue.NIL;
        Varargs data = fromValueWithoutMetadata(var);
        if (var.hasMetaTable() && !var.instanceOf(VarType.TUPLE)){
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
                    Varargs rawKey = fromValue(key.pack());
                    assert rawKey instanceof LuaValue;
                    Varargs rawValue = fromValue(value.pack());
                    assert rawValue instanceof LuaValue;
                    newTable.set((LuaValue) rawKey, (LuaValue) rawValue);
                });
                return newTable;
            case LIST, TUPLE:
                assert var.getValue() instanceof List;
                int size = ((List) var.getValue()).size();
                LuaValue[] values = new LuaValue[size];
                for (int i = 0; i < size; i++){
                    Varargs rawValue = fromValue(((List) var.pack().getValue()).get(i));
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
                        ParameterCheckReturn check = ParameterRules.checkParameters(values, rules, null);
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
            case EXCEPTION:
                assert var.getValue() instanceof Exception;
                return LuaValue.error(var.getValue().toString());
            case NULL:
                return LuaValue.NIL;
        }
        return LuaValue.NIL;
    }

    @Override
    public Value<?> toValue(Varargs var){
        if (var instanceof LuaNil) return Value.NULL;
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
            return Value.of(new Tuple(values));
        }
        assert var instanceof LuaValue;
        LuaValue val = (LuaValue) var;
        if (val instanceof LuaInteger){
            return Value.of(val.toint());
        }else if (val instanceof LuaDouble){
            return Value.of(val.todouble());
        }else if (val instanceof LuaBoolean){
            return Value.of(val.toboolean());
        }else if (val instanceof LuaString){
            return Value.of(val.toString());
        }else if (val instanceof LuaTable table){
            Table tabll = new Table();
            Value[] vals = new Value[table.length()];
            boolean isList = true;
            LuaValue key = LuaValue.NIL;
            while (true){
                Varargs out = table.next(key);
                if (out==LuaValue.NIL) break;
                key = out.arg1();
                LuaValue value = out.arg(2);
                if (isList && key.isint()){
                    if (key.toint()>vals.length || key.toint() < 1){
                        isList = false;
                    }else{
                        vals[key.toint()-1] = toValueWithoutMetadata(value);
                    }
                }else{
                    isList = false;
                }
                tabll.put(toValueWithoutMetadata(key), toValueWithoutMetadata(value));
            }
            return isList ? Value.of(vals) : Value.of(tabll);
        }else if (val instanceof LuaFunction function){
            return Value.of(new Function(ParameterRules.ANY) {
                @Override
                public Value call(FunctionInput parameters) {
                    try{
                        LuaValue[] values = new LuaValue[parameters.getSize()];
                        for (int i = 0; i < parameters.getSize(); i++){
                            Varargs rawValue = fromValue(parameters.get(i).pack());
                            values[i] = (LuaValue) rawValue;
                        }
                        Varargs output = function.invoke(values);
                        return toValue(output);
                    }catch (java.lang.Exception e){
                        return Value.of(new Exception(e.getMessage()));
                    }
                }
            });
        }
        return Value.NULL;
    }
}
