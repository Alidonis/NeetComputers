package com.redtoast.external;

import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.Exposable;

import java.util.UUID;

public interface PeripheralProvider {
    boolean kill(PeripheralConsumer peripheralConsumer);
    Exposable getAPI(PeripheralConsumer peripheralConsumer);
    void tick(PeripheralConsumer peripheralConsumer, short deltaTime);
    default boolean impermeable(){return false;}
    UUID getProviderUuid();
    String getType();
}
