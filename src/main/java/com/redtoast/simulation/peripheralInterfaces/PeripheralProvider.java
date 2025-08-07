package com.redtoast.simulation.peripheralInterfaces;

import com.redtoast.simulation.base.Peripheral;
import com.redtoast.simulation.connectionManager.ConnectionProvider;

public interface PeripheralProvider extends ConnectionProvider {
    Peripheral generateAPI(PeripheralConsumer source);
    boolean isAccessible(PeripheralConsumer source);
}
