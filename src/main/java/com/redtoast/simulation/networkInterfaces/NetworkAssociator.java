package com.redtoast.simulation.networkInterfaces;

public interface NetworkAssociator<T, R> {
    R onConnection(T source);
}
