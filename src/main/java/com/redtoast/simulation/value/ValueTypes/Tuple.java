package com.redtoast.simulation.value.ValueTypes;

import com.redtoast.simulation.value.Value;

import java.util.LinkedList;

/**
 * represents an N.E.E.T. computers tuple, interchangeable with and extends {@link List}
 * <p>
 *     A tuple represents a collection of 'unpacked' elements, however in code this depiction is identical to a {@link List} except for how
 *     languages are supposed to handle it
 * </p>
 * <p>
 *     {@code a, b = IReturnATuple();}
 * </p>
 * <p>
 *     {@code function ITakeATuple(tuple...){}}
 * </p>
 * <p>
 *     {@code ITakeATuple(1, 2, 3);}
 * </p>
 * <p>
 *     The 'Tuple' nomenclature is borrowed from <i>python</i>'s (i do not endorse python's use) representation of the same concept
 * </p>
 *
 * @see Value
 * @see List
 * @see Table
 * @see Function
 * @see Exception
 * @see java.util.LinkedList
 */
public class Tuple extends List{
    public Tuple(Value<?>[] values) {
        super(values);
    }

    public Tuple(LinkedList<Value> values) {
        super(values);
    }
    public Tuple(){super();}
    public Tuple(Object... values){super(values);}
    @Override
    public boolean isPacked(){return true;}
}
