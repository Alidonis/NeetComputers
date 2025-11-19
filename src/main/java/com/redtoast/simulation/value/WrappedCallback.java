package com.redtoast.simulation.value;

import com.redtoast.simulation.value.ValueTypes.ControlType;

import java.util.UUID;

public interface WrappedCallback {
    UUID getUuid();
    ControlCallback getCallback();
    void complete(Object trigger);
}
