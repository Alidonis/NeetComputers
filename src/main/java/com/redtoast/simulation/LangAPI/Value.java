package com.redtoast.simulation.LangAPI;

import com.redtoast.simulation.LangAPI.ValueTypes.*;

import java.util.LinkedList;

public class Value<Type> {
    public final static Value<Null> NULL = new Value<>(new Null());
    public final static Value<Boolean> TRUE = new Value<>(true);
    public final static Value<Boolean> FALSE = new Value<>(false);

    private Type value;
    private VarType type = VarType.NULL;
    private Table metaTable = null;
    private boolean hasMetadata = false;
    public Value(Type val){
        if (val instanceof Integer){
            type = VarType.INT;
        }if (val instanceof Long){
            type = VarType.INT;
        }else if (val instanceof Double){
            type = VarType.DOUBLE;
        }else if (val instanceof Float){
            type = VarType.FLOAT;
        }else if (val instanceof Boolean){
            type = VarType.BOOLEAN;
        }else if (val instanceof String){
            type = VarType.STRING;
        }else if (val instanceof Table){
            type = VarType.TABLE;
        }else if (val instanceof Tuple){
            type = VarType.TUPLE;
        }else if (val instanceof List){
            type = VarType.LIST;
        }else if (val instanceof Function){
            type = VarType.FUNCTION;
        }else if (val instanceof Exemption){
            type = VarType.EXEMPTION;
        }
        value = val;
    }

    public static Value<List> toList(Value<?>[] values){
        return new Value<>(new List(values));
    }
    public static Value<List> toList(LinkedList<Value> values){
        return new Value<>(new List(values));
    }

    public static Value<Tuple> toTuple(Value<?>[] values){
        return new Value<>(new Tuple(values));
    }
    public static Value<Tuple> toTuple(LinkedList<Value> values){
        return new Value<>(new Tuple(values));
    }

    public static Value<Exemption> asError(String message){
        return new Value<>(new Exemption(message));
    }

    public Integer toInt(){
        if (instanceOf(VarType.NUMBER)){
            switch (type){
                case INT:
                    return (Integer) value;
                case DOUBLE:
                    return ((Double) value).intValue();
                case FLOAT:
                    return ((Float) value).intValue();
            }
            return null;
        }else{
            return null;
        }
    }
    public Double toDouble(){
        if (instanceOf(VarType.NUMBER)){
            switch (type){
                case INT:
                    return ((Integer) value).doubleValue();
                case DOUBLE:
                    return (Double) value;
                case FLOAT:
                    return ((Float) value).doubleValue();
            }
            return null;
        }else{
            return null;
        }
    }
    public Float toFloat(){
        if (instanceOf(VarType.NUMBER)){
            switch (type){
                case INT:
                    return ((Integer) value).floatValue();
                case DOUBLE:
                    return ((Double) value).floatValue();
                case FLOAT:
                    return (Float) value;
            }
            return null;
        }else{
            return null;
        }
    }
    public Boolean toBool(){
        if (instanceOf(VarType.BOOLEAN)){
            return (boolean) value;
        }else{
            return null;
        }
    }
    public String toString(){
        if (instanceOf(VarType.STRING)){
            return (String) value;
        }else{
            return null;
        }
    }
    public List toList(){
        if (instanceOf(VarType.LIST)){
            return (List) value;
        }else{
            return null;
        }
    }
    public Table toTable(){
        if (instanceOf(VarType.TABLE)){
            return (Table) value;
        }else{
            return null;
        }
    }

    public VarType getType(){
        return type;
    }

    public void setMetaTable(Table metadata){
        metaTable = metadata;
        hasMetadata = true;
    }
    public Table getMetaTable(){
        return metaTable;
    }
    public boolean hasMetadata(){
        return hasMetadata;
    }
    public void setMeta(String key, Value value){
        metaTable.put(key, value);
    }
    public void setMeta(String key, String value){
        metaTable.put(key, new Value(value));
    }
    public Value getMeta(String key){
        if (!metaTable.contains(key)){
            return Value.NULL;
        }
        return metaTable.get(key);
    }

    public boolean instanceOf(VarType comparison){
        if (comparison==VarType.ALL) return true;
        if (comparison==VarType.NUMBER && type==VarType.INT) return true;
        if (comparison==VarType.NUMBER && type==VarType.DOUBLE) return true;
        if (comparison==VarType.NUMBER && type==VarType.FLOAT) return true;
        return comparison==type;
    }

    public Type getValue() {
        return value;
    }

    public boolean isNull(){
        return type == VarType.NULL;
    }

    public String typeName(){
        return VarName(type);
    }

    public Value serialize(){
        if (type==VarType.TUPLE){
            Value newVal = this;
            newVal.type = VarType.LIST;
            return newVal;
        }else{
            return this;
        }
    }

    public static String VarName(VarType type){
        switch (type){
            case INT:
                return "int";
            case DOUBLE:
                return "double";
            case FLOAT:
                return "float";
            case NUMBER:
                return "number";
            case BOOLEAN:
                return "boolean";
            case STRING:
                return "string";
            case TABLE:
                return "table";
            case LIST:
                return "list";
            case TUPLE:
                return "tuple";
            case FUNCTION:
                return "function";
            case EXEMPTION:
                return "exemption";
            case ALL:
                return "all";
        }
        return "null";
    }
}