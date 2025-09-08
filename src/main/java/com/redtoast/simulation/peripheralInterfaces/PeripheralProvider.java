package com.redtoast.simulation.peripheralInterfaces;

import com.redtoast.simulation.connectionManager.ConnectionProvider;

public interface PeripheralProvider extends ConnectionProvider {
    com.redtoast.external.PeripheralProvider generateAPI(PeripheralConsumer source);
    boolean isAccessible(PeripheralConsumer source);
}
