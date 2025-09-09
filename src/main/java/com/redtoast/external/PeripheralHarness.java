package com.redtoast.external;

public interface PeripheralHarness {
    void attached(RuntimePeripheralContainer peripheral);
    void detached(RuntimePeripheralContainer peripheral);
    void tick(short deltaTime);
}
