package com.redtoast.simulation.value;

public enum VarType {
    INT(true, true),
    DOUBLE(true, true),
    FLOAT(true, true),
    NUMBER(true, true),
    BOOLEAN(false, true),
    STRING(false, true),
    NULL(false, false),
    EXCEPTION(false, false),
    FUNCTION(false, false),
    TABLE(false, false),
    LIST(false, false),
    TUPLE(false, false),
    BYTES(false, true),
    BINARY(false, false),
    PRIMITIVE(false, true),
    ANY(false, false);

    private final boolean isNumber;
    private final boolean isPrimitive;

    VarType(boolean isNumber, boolean isPrimitive) {
        this.isNumber = isNumber;
        this.isPrimitive = isPrimitive;
    }

    public boolean isNumber() {
        return isNumber;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}