package com.redtoast.simulation.value;

import com.redtoast.simulation.value.ValueTypes.Table;
import org.jetbrains.annotations.Nullable;

public interface ComplexValue<Type> {
    /**
     * encapsulates the complex value
     * @return encapsulated value
     */
    Value<Type> asValue();
    /**
     * sets the values internal metatable
     * @param metadata new table
     */
    void setMetaTable(Table metadata);
    /**
     * retrieves the internal metatable or returns null
     * @return Table or null
     */
    @Nullable Table getMetaTable();
    /**
     * determines if this value encapsulates a internal metatable (istg this function is haunted)
     * @return boolean
     */
    boolean hasMetaTable();
    /**
     * sets individual value in the internal metatable or does nothing if no metatable is present
     * @param key key determining were the new value is stored
     * @param value new value
     */
    void setMeta(Object key, Object value);
    /**
     * gets the value associated with the given key in the internal metatable or returns null if no metatable is present
     * @param key key determining were the new value is read
     * @return Value or null
     */
    Value getMeta(Object key);
}
