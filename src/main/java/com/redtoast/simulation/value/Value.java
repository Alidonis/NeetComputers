package com.redtoast.simulation.value;

import com.redtoast.simulation.value.ValueTypes.*;
import com.redtoast.simulation.value.ValueTypes.Exception;
import com.redtoast.simulation.value.ValueTypes.Function;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

/**
 * The standard N.E.E.T. computer representation of a generic value
 * <p>
 *     a Value instance encapsulates an instance from its parameterized type, excepts all primitives and the following complex classes
 * </p>
 * <ul>
 *     <li>{@link String}</li>
 *     <li>{@link List}</li>
 *     <li>{@link Tuple}</li>
 *     <li>{@link Table}</li>
 *     <li>{@link Function}</li>
 *     <li>{@link Exception}</li>
 *     <li>Value[]</li>
 *     <li>Any list of values</li>
 * </ul>
 * @param <Type> the class the Value is encapsulating
 * @see #of(Object) encapsulation method
 * @see VarType type enum
 * @see List standered list
 * @see Tuple standered tuple
 * @see Table standered table
 * @see Function standered function
 * @see Exception standered error
 */
public class Value<Type> {
    /**
     * static Value representation of null
     */
    public final static Value<Null> NULL = new Value<>(new Null());
    /**
     * static Value representation of true
     */
    public final static Value<Boolean> TRUE = Value.of(true);
    /**
     * static Value representation of false
     */
    public final static Value<Boolean> FALSE = Value.of(false);

    private Type value;
    private VarType type = VarType.NULL;
    private Table metaTable = null;
    private boolean hasMetadata = false;

    /**
     * initializes the value raw with no type protection, it's advisable to use {@link #of(Object)} instead
     * @param val value to encapsulate
     */
    @Deprecated
    public Value(Type val){
        if (val instanceof Integer){
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
        }else if (val instanceof Exception){
            type = VarType.EXCEPTION;
        }
        if (val instanceof ComplexValue<?> complexValue){
            metaTable = complexValue.getMetaTable();
        }
        value = val;
    }

    /**
     * encapsulates the provided value into a Value object
     * <p>
     *     works as a type-protected wrapper for the {@link #Value} constructor
     * </p>
     * @param value value to encapsulate
     * @return Value
     */
    public static Value<?> of(Object value){
        return switch (value) {
            case null -> NULL;
            case Value<?> val -> val;
            case Long val -> new Value<>((int) (long) val);
            case Short val -> new Value<>((int) (short) val);
            case Character val -> new Value<>(String.valueOf(val));
            case Value[] val -> Value.of(val);
            case java.util.List<?> val -> Value.of(val);
            default -> new Value<>(value);
        };
    }
    /**
     * bulk converts a varible amount of args into a encapsulated {@link List}
     * @param values values to encapsulate
     * @return Value containing List
     */
    public static Value<List> of(Object... values){
        LinkedList<Value> vals = new LinkedList<>();
        for (Object obj : values){
            vals.add(of(obj));
        }
        return of(vals);
    }
    public static Value<Integer> of(short value){
        return new Value<>((int) value);
    }
    public static Value<Integer> of(long value){
        return new Value<>((int) value);
    }
    public static Value<Integer> of(int value){
        return new Value<>(value);
    }
    public static Value<Double> of(double value){
        return new Value<>(value);
    }
    public static Value<Float> of(float value){
        return new Value<>(value);
    }
    public static Value<Boolean> of(boolean value){
        return new Value<>(value);
    }
    public static Value<String> of(String value){
        return new Value<>(value);
    }
    public static Value<String> of(char value){
        return new Value<>(String.valueOf(value));
    }
    public static Value<Table> of(Table value){
        return new Value<>(value);
    }
    public static Value<List> of(List value){
        return new Value<>(value);
    }
    public static Value<Tuple> of(Tuple value){
        return new Value<>(value);
    }
    public static Value<Function> of(Function value){
        return new Value<>(value);
    }
    public static Value<Exception> of(Exception value){
        return new Value<>(value);
    }
    public static <T> Value<T> of (Value<T> value) {return value;}
    public static Value<List> of(Value[] values){
        return new Value<>(new List(values));
    }
    public static Value<List> of(java.util.List<Value> values){
        LinkedList<Value> list = new LinkedList<>();
        list.addAll(values);
        return new Value<>(new List(list));
    }
    public static Value<Null> of(){
        return NULL;
    }

    /**
     * generates an encapsulated Exception (NC version not java version) object
     * @param message message to use as error
     * @return encapsulated exception
     */
    public static Value<Exception> asError(String message){
        return Value.of(new Exception(message));
    }

    /**
     * returns value with the modifier that if the value is a list it will be cast to a tuple
     * <p>
     *     <i>the returned value is never a list</i>
     * </p>
     * @return modified value
     * @see #pack()
     */
    public Value unpack(){
        if (type==VarType.LIST){
            return this.toTuple().asValue();
        }else{
            return this;
        }
    }

    /**
     * returns value with the modifier that if the value is a tuple it will be cast to a list
     * <p>
     *     <i>the returned value is never a tuple</i>
     * </p>
     * @return modified value
     * @see #unpack()
     */
    public Value pack(){
        if (type==VarType.TUPLE){
            return this.toList().asValue();
        }else{
            return this;
        }
    }

    /**
     * casts the value as an integer or returns nothing if the encapsulated value is not integer cast-able
     * @return Integer or null
     */
    public @Nullable Integer toInt(){
        if (instanceOf(VarType.NUMBER)){
            switch (type){
                case INT:
                    return ((Integer) value);
                case DOUBLE:
                    return (int)Math.floor((Double) value);
                case FLOAT:
                    return (int)Math.floor((Float) value);
            }
            return null;
        }else{
            return null;
        }
    }
    /**
     * casts the value as a double or returns nothing if the encapsulated value is not double cast-able
     * @return Double or null
     */
    public @Nullable Double toDouble(){
        if (instanceOf(VarType.NUMBER)){
            switch (type){
                case INT:
                    return (double)((Integer) value);
                case DOUBLE:
                    return (Double) value;
                case FLOAT:
                    return (double)((Float) value);
            }
            return null;
        }else{
            return null;
        }
    }
    /**
     * casts the value as a float or returns nothing if the encapsulated value is not float cast-able
     * @return Float or null
     */
    public @Nullable Float toFloat(){
        if (instanceOf(VarType.NUMBER)){
            switch (type){
                case INT:
                    return (float)((Integer) value);
                case DOUBLE:
                    return (float)((double) value);
                case FLOAT:
                    return (Float) value;
            }
            return null;
        }else{
            return null;
        }
    }
    /**
     * de-encapsulates the internal value as a boolean
     * @return Boolean or null
     */
    public @Nullable Boolean toBool(){
        if (instanceOf(VarType.BOOLEAN)){
            return (boolean) value;
        }else{
            return null;
        }
    }
    /**
     * de-encapsulates the internal value as a String
     * @return String or null
     * @see #asString() get as string instead of de-encapsulating
     */
    public @Nullable String toString(){
        if (instanceOf(VarType.STRING)){
            return (String) value;
        }else{
            return null;
        }
    }
    /**
     * casts the value as a list or returns nothing if the encapsulated value is not list cast-able
     * @return List or null
     */
    public @Nullable List toList(){
        if (value instanceof List){
            if (value instanceof Tuple tup){
                return new List(tup.toArray());
            }else{
                return (List) getValue();
            }
        }else{
            return null;
        }
    }
    /**
     * casts the value as a tuple or returns nothing if the encapsulated value is not tuple cast-able
     * @return Tuple or null
     */
    public @Nullable Tuple toTuple(){
        if (value instanceof List){
            if (value instanceof Tuple tup){
                return tup;
            }else{
                return new Tuple(((List) getValue()).toArray());
            }
        }else{
            return null;
        }
    }
    /**
     * de-encapsulates the internal value as a table
     * @return Table or null
     */
    public @Nullable Table toTable(){
        if (instanceOf(VarType.TABLE)){
            return (Table) getValue();
        }else{
            return null;
        }
    }

    /**
     * gets the type enum for this value
     * @return VarType
     */
    public VarType getType(){
        return type;
    }

    /**
     * sets the values internal metatable
     * @param metadata new table
     */
    public void setMetaTable(Table metadata){
        metaTable = metadata;
        hasMetadata = true;
    }

    /**
     * retrieves the internal metatable or returns null
     * @return Table or null
     */
    public @Nullable Table getMetaTable(){
        return metaTable;
    }

    /**
     * determines if this value encapsulates a internal metatable (istg this function is haunted)
     * @return boolean
     */
    public boolean hasMetaTable(){
        return hasMetadata && metaTable!=null;
    }

    /**
     * sets individual value in the internal metatable or does nothing if no metatable is present
     * @param key key determining were the new value is stored
     * @param value new value
     */
    public void setMetaTable(String key, Value value){
        if (metaTable==null) return;
        metaTable.put(key, value);
    }
    /**
     * sets individual value in the internal metatable or does nothing if no metatable is present
     * @param key key determining were the new value is stored
     * @param value new value
     */
    public void setMetaTable(String key, String value){
        if (metaTable==null) return;
        metaTable.put(key, Value.of(value));
    }

    /**
     * gets the value associated with the given key in the internal metatable or returns null if no metatable is present
     * @param key key determining were the new value is read
     * @return Value or null
     */
    public @Nullable Value getMetaTable(String key){
        if (metaTable==null) return null;
        if (!metaTable.contains(key)){
            return Value.NULL;
        }
        return metaTable.get(key);
    }

    /**
     * tests to see if the values type matches the provided comparison type with exceptions for .Number and .ANY types
     * @param comparison the VarType to compare with
     * @return the result of the test preformed
     */
    public boolean instanceOf(VarType comparison){
        if (comparison==VarType.ANY) return true;
        if (comparison==VarType.PRIMITIVE){
            switch (type){
                case NULL, FLOAT, INT, DOUBLE, STRING, BOOLEAN, TABLE, LIST: return true;
            }
        }
        if (comparison==VarType.NUMBER && type==VarType.INT) return true;
        if (comparison==VarType.NUMBER && type==VarType.DOUBLE) return true;
        if (comparison==VarType.NUMBER && type==VarType.FLOAT) return true;
        return comparison==type;
    }

    /**
     * returns the non-Type protected direct value the instance was encapsulating
     * @return the raw value
     */
    @Deprecated
    public Type getValue() {
        if (value instanceof ComplexValue<?> complexValue){
            complexValue.setMetaTable(metaTable);
        }
        return value;
    }

    /**
     * checks if this object represents a null instance
     * @return the state of the check
     */
    public boolean isNull(){
        return type == VarType.NULL;
    }

    /**
     * returns the type of this instance as a string
     * @return type this Value represents
     */
    public String typeName(){
        return VarName(type);
    }

    /**
     * represents a VarType enum as a string
     * @param type the enum being evaluated
     * @return name of the type
     */
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
            case EXCEPTION:
                return "exemption";
            case PRIMITIVE:
                return "primitive";
            case ANY:
                return "all";
        }
        return "null";
    }

    /**
     * creates a string visualising the value, does not return encapsulated string values, see {@link String toString()}
     * @return string form of value
     */
    public String asString(){
        if (type==VarType.NULL) return "Value of <null>";
        return "Value of <"+typeName()+' '+getValue().toString()+'>';
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof Value<?> _value){
            return _value.getValue().equals(value);
        }
        return super.equals(obj);
    }
}