package com.redtoast.simulation.peripheralInterfaces;

import com.redtoast.external.PeripheralProvider;
import com.redtoast.simulation.connectionManager.ConnectionConsumer;

public interface PeripheralConsumer extends ConnectionConsumer {
    void onPeripheralAttached(PeripheralProvider api, com.redtoast.simulation.peripheralInterfaces.PeripheralProvider source);
    void onPeripheralDetached(com.redtoast.simulation.peripheralInterfaces.PeripheralProvider source);
    boolean canAcceptPeripherals(com.redtoast.simulation.peripheralInterfaces.PeripheralProvider source);
}
