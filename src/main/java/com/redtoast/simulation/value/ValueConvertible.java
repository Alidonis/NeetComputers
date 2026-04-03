package com.redtoast.simulation.value;

public interface ValueConvertible<type> {
    Value<type> asValue();
}
