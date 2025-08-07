package com.redtoast.simulation.networkInterfaces;

import com.redtoast.simulation.connectionManager.ConnectionProvider;

public interface NetworkProvider extends ConnectionProvider {
    boolean isAccessible(NetworkConsumer source);
}
