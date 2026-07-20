package com.redtoast.YSLua;

import com.redtoast.simulation.base.LanguageTranslater;
import com.redtoast.simulation.parameter.FunctionInput;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Function;
import com.redtoast.simulation.value.ValueTypes.List;
import com.redtoast.simulation.value.ValueTypes.Table;
import com.redtoast.simulation.value.ValueTypes.Tuple;
import com.redtoast.simulation.value.VarType;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

public class LuaTranslater implements LanguageTranslater<LuaValue, LuaValue> {
    @Override
    public Value<?> toValue(LuaValue var) {
        if (var==null) return Value.NULL;
        switch (var.getType()){
            case NIL -> {
                return Value.NULL;
            }
            case BOOLEAN -> {
                return Value.of((boolean) var.getValue());
            }
            case NUMINT -> {
                return Value.of((int) var.getValue());
            }
            case NUMFLOAT -> {
                return Value.of((float) var.getValue());
            }
            case STRING -> {
                return Value.of((String) var.getValue());
            }
            case FUNCTION -> {
                return var.getValue() instanceof LuaFunction function ? Value.of(function.function) : Value.NULL;
            }
            case LIST -> {
                List list = new List();
                for (LuaValue value : (LuaValue[]) var.getValue()) list.add(toValue(value));
                return list.asValue();
            }
            case TABLE -> {
                Table table = new Table();
                ((Map<LuaValue, LuaValue>) var.getValue()).forEach((key, value) -> table.put(toValue(key), toValue(value)));
                return table.asValue();
            }
            default -> {
                return Value.NULL;
            }
        }
    }

    @Override
    public LuaValue fromValue(Value<?> var) {
        if (var==null) return LuaValue.from();
        switch (var.getType()){
            case INT -> {
                return LuaValue.from((int) var.getValue());
            }
            case DOUBLE, FLOAT -> {
                return LuaValue.from((float) var.getValue());
            }
            case BOOLEAN -> {
                return LuaValue.from((boolean) var.getValue());
            }
            case STRING -> {
                return LuaValue.from(var.toString());
            }
            case NULL -> {
                return LuaValue.from();
            }
            case EXCEPTION -> {
                return LuaValue.error(var.getValue().toString());
            }
            case FUNCTION -> {
                return LuaValue.from(new LuaFunction(var.toFunction()));
            }
            case TABLE -> {
                Table table = var.toTable();
                Map<LuaValue, LuaValue> map = new Hashtable<>();
                table.foreach((key, value) -> map.put(fromValue(key), fromValue(value)));
                return LuaValue.from(map);
            }
            case LIST,TUPLE -> {
                List list = var.toList();
                LuaValue[] array = new LuaValue[list.size()];
                for (int i = 0; i < array.length; i++) array[i] = fromValue(list.get(i));
                return LuaValue.from(array);
            }
            default -> {
                return LuaValue.invalid();
            }
        }
    }

    private class LuaFunction implements LuaValue.Function {
        private final Function function;

        public LuaFunction(Function function) {
            this.function = function;
        }

        @Override
        public LuaValue[] call(LuaValue[] parameters) {
            LinkedList<Value> args = new LinkedList<>();
            for (LuaValue value : parameters) args.add(LuaTranslater.this.toValue(value));
            Value<?> retrn = function.invoke(new FunctionInput(args));
            if (retrn.isNull()) return new LuaValue[0];
            if (retrn.instanceOf(VarType.TUPLE)) {
                Tuple tuple = retrn.toTuple();
                LuaValue[] returns = new LuaValue[tuple.size()];
                for (int i = 0; i < tuple.size(); i++)
                    returns[i] = LuaTranslater.this.fromValue(tuple.get(i));
                return returns;
            }
            return new LuaValue[]{LuaTranslater.this.fromValue(retrn)};
        }

        public Function getFunction() {return function;}
    }
}
