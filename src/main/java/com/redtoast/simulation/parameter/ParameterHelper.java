package com.redtoast.simulation.parameter;

import com.google.gson.internal.Primitives;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.*;
import com.redtoast.simulation.value.VarType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Parameter;

public class ParameterHelper {


    public record ParameterType(VarType type, VarType filter, int depth, Annotation[] annotations) {
        public boolean canCast(Value<?> value) {
            if (type == VarType.LIST || type == VarType.TUPLE) {
                if (!value.canCast(type)) return false;
                return iterate(1, value);
            }else{
                if (type==VarType.ANY) return true;
                return value.canCast(type, annotations);
            }
        }

        private boolean iterate(int i, Value<?> value) {
            if (value.getType() != VarType.LIST && value.getType() != VarType.TUPLE) return false;
            for (Value<?> val : value.toList()) {
                if (i < depth){
                    if (val.getType() != VarType.LIST && val.getType() != VarType.TUPLE) return false;
                    boolean retrn = iterate(i+1, val);
                    if (!retrn) return false;
                }else{
                    if (filter!=VarType.ANY && !val.canCast(filter)) return false;
                }
            }
            return true;
        }

        @Override
        public String toString(){
            StringBuilder name;
            if (depth==0) {
                name = new StringBuilder(type.toString());
            }else{
                name = new StringBuilder(filter.toString());
                name.append("[]".repeat(depth));
            }
            return name.toString();
        }
    }

    public static Object cast(Value<?> value, Class<?> clazz, boolean pack, Annotation[] annotations) {
        if (clazz == int.class) return checkExists(value.castTo(VarType.INT, annotations).getValue());
        if (clazz == double.class) return checkExists(value.castTo(VarType.DOUBLE, annotations).getValue());
        if (clazz == float.class) return checkExists(value.castTo(VarType.FLOAT, annotations).getValue());
        if (clazz == boolean.class) return checkExists(value.castTo(VarType.BOOLEAN, annotations).getValue());
        if (clazz == Integer.class) return checkExists(value.castTo(VarType.INT, annotations).getValue());
        if (clazz == Double.class) return checkExists(value.castTo(VarType.DOUBLE, annotations).getValue());
        if (clazz == Float.class) return checkExists(value.castTo(VarType.FLOAT, annotations).getValue());
        if (clazz == Boolean.class) return checkExists(value.castTo(VarType.BOOLEAN, annotations).getValue());
        if (clazz == byte[].class) return checkExists(value.castTo(VarType.BYTES, annotations).toBytes().getData());
        if (clazz == Bytes.class) return checkExists(value.castTo(VarType.BYTES, annotations).getValue());
        if (clazz == char[].class) return checkExists(value.castTo(VarType.STRING, annotations).toString().toCharArray());
        if (clazz == String.class) return checkExists(value.castTo(VarType.STRING, annotations).getValue());
        if (clazz == Table.class) return checkExists(value.castTo(VarType.TABLE, annotations).getValue());
        if (clazz == Function.class) return checkExists(value.castTo(VarType.FUNCTION, annotations).getValue());
        if (clazz == Object.class) return checkExists(value.getValue());
        if (clazz == List.class) return checkExists(value.castTo(VarType.LIST, annotations).getValue());
        if (clazz == Tuple.class) return checkExists(value.castTo(VarType.TUPLE, annotations).getValue());
        if (clazz.isArray()) {
            List list = (List) checkExists((pack ? value.castTo(VarType.TUPLE, annotations) : value.castTo(VarType.LIST, annotations)).getValue());
            Class<?> sub = Primitives.wrap(clazz.componentType());
            Object[] array = (Object[]) Array.newInstance(sub, list.size());
            for (int i = 0; i < list.size(); i++) {
                array[i] = cast(list.get(i), sub, false, annotations);
            }
            return array;
        }
        throw new IllegalStateException("Attempted to cast unrecognized parameter type");
    }

    private static Object checkExists(Object value) {
        if (value == null || value instanceof Null) throw new IllegalArgumentException("Attempted to cast unrecognized parameter type");
        return value;
    }

    public static Parameters deduceParameters(Parameter[] parameters) {
        ParameterType[] parameterTypes = new ParameterType[parameters.length];
        Class<?>[] classes = new Class[parameters.length];
        boolean varargs = false;
        for (int i = 0; i < parameters.length; i++) {
            try{
                parameterTypes[i] = configureList(inferType(parameters[i].getType(), i == parameters.length-1, 0, parameters[i].getAnnotations()), parameters[i].getType(), parameters[i].isVarArgs());
                classes[i] = parameters[i].getType();
                if (i == parameters.length-1) varargs = parameters[i].isVarArgs() && parameterTypes[i].depth>0 || parameterTypes[i].type==VarType.TUPLE;
            } catch (IllegalStateException ignored){
                throw new IllegalArgumentException("Failed to recognize parameter type "+parameters[i].getType().toString()+" at parameter #"+i);
            }
        }
        return new Parameters(parameterTypes, classes, varargs);
    }

    private static ParameterType configureList(ParameterType type, Class<?> clazz, boolean tuple) {
        if (type.depth>0 && type.filter==VarType.NULL) {
            return new ParameterType(tuple ? VarType.TUPLE : VarType.LIST, type.type(), type.depth, type.annotations);
        }
        return type;
    }

    private static ParameterType inferType(Class<?> clazz, boolean allowTuple, int entryDepth, Annotation[] annotations) {
        VarType type = VarType.NULL;
        int depth = entryDepth;
        VarType filter = VarType.NULL;
        if (entryDepth>0 && Primitives.isPrimitive(clazz)) throw new IllegalArgumentException("Parameter arrays cant be primitive I.E (int[] should be Integer[])");
        if (clazz == int.class || clazz == Integer.class) type = VarType.INT;
        if (clazz == double.class || clazz == Double.class) type = VarType.DOUBLE;
        if (clazz == float.class || clazz == Float.class) type = VarType.FLOAT;
        if (clazz == boolean.class || clazz == Boolean.class) type = VarType.BOOLEAN;
        if (clazz == byte[].class || clazz == Bytes.class) type = VarType.BYTES;
        if (clazz == char[].class || clazz == String.class) type = VarType.STRING;
        if (clazz == List.class) {
            type = VarType.LIST;
            depth++;
            filter = VarType.ANY;
        }
        if (clazz == Tuple.class) {
            type = allowTuple ? VarType.TUPLE : VarType.LIST;
            depth++;
            filter = VarType.ANY;
        }
        if (clazz == Table.class) type = VarType.TABLE;
        if (clazz == Function.class) type = VarType.FUNCTION;
        if (clazz == Object.class) type = VarType.ANY;
        if (type == VarType.NULL) {
            if (clazz.isArray()) {
                return inferType(clazz.componentType(), allowTuple, depth + 1, annotations);
            }
            throw new IllegalStateException("Attempted to parse unrecognized parameter type");
        }
        return new ParameterType(type, filter, depth, annotations);
    }
}
