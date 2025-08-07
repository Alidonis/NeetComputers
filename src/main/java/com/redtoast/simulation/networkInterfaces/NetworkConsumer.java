package com.redtoast.simulation.networkInterfaces;

import com.redtoast.simulation.base.API;
import com.redtoast.simulation.connectionManager.ConnectionConsumer;
import com.redtoast.simulation.peripheralInterfaces.PeripheralProvider;

public interface NetworkConsumer extends ConnectionConsumer {
    void onNetworkAttached(NetworkProvider source);
    void onNetworkDetached(NetworkProvider source);
    boolean canAcceptNetworks(NetworkProvider source);
}
