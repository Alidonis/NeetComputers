package com.redtoast.simulation.base;

import com.redtoast.simulation.value.Value;

import java.util.UUID;

public interface GlobalGeneric {
    void insert(Value key, Value value);
    String getLang();
    UUID getUUID();
}
