package com.redtoast.external;

import java.util.UUID;

public interface PeripheralConsumer {
    void attachPeripheral(PeripheralProvider peripheral);

    boolean detachPeripheral(UUID peripheralProviderUuid);

    boolean isDead();

    UUID getUuid();
}
