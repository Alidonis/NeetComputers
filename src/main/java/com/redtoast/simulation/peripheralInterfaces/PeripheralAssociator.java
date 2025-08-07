package com.redtoast.simulation.peripheralInterfaces;

public interface PeripheralAssociator<T, R> {
    R onConnection(T source);
}
