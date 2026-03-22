package com.redtoast.Connections;

import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.base.ExposedError;
import com.redtoast.simulation.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface PeripheralProvider {
    String[] getFunctionNames();
    Value<?> callFunction(Runtime runtime, String name, Value<?>... Args);
    String getTypeName();
    UUID getUuid();
    @Nullable String getTag();
    void setTag(@NotNull String tag);
}
