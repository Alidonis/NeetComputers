package com.redtoast.external;

import java.util.UUID;

public interface PeripheralConsumer {
    default void attachPeripheral(PeripheralProvider peripheral){
        attachPeripheral(new TokenizedPeripheral(this, peripheral));
    }

    void attachPeripheral(TokenizedPeripheral peripheral);

    boolean detachPeripheral(UUID peripheralProviderUuid);

    boolean isDead();

    UUID getUuid();
}
