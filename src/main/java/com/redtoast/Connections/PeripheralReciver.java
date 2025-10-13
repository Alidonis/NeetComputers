package com.redtoast.Connections;

import com.redtoast.simulation.value.Value;

import java.util.List;
import java.util.UUID;

public interface PeripheralReciver {
    List<PeripheralProvider> scanForPeripherals();
    Value<?> sendFunctionCall(UUID uuid, String functionName, Value<?>... args);
}
