package com.redtoast.simulation.peripheralInterfaces;

import com.redtoast.simulation.base.Peripheral;
import com.redtoast.simulation.connectionManager.ConnectionConsumer;

public interface PeripheralConsumer extends ConnectionConsumer {
    void onPeripheralAttached(Peripheral api, PeripheralProvider source);
    void onPeripheralDetached(PeripheralProvider source);
    boolean canAcceptPeripherals(PeripheralProvider source);
}
