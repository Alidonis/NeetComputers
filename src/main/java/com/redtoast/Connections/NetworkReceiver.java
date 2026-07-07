package com.redtoast.Connections;

import com.redtoast.simulation.value.Value;

import java.util.UUID;

public interface NetworkReceiver {
    void receiveNetwork(Value<?>... args);
    UUID getUuid();
}
