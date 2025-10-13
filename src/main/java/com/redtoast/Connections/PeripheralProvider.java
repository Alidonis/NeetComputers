package com.redtoast.Connections;

import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.value.Value;

import java.util.UUID;

public interface PeripheralProvider {
    String[] getFunctionNames();
    Value<?> callFunction(String name, Value<?>... Args);
    String getTypeName();
    UUID getUuid();
}
