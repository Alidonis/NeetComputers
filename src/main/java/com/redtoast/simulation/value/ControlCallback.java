package com.redtoast.simulation.value;

/**
 * Callback for {@link com.redtoast.simulation.value.ValueTypes.ControlType}, can run code or return a value
 */
@FunctionalInterface
public interface ControlCallback {
    Object apply();
}
