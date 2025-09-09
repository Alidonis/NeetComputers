package com.redtoast.external;

import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.base.API;
import com.redtoast.simulation.base.Exposable;

import java.util.UUID;

public class TokenizedPeripheral {
    public final PeripheralConsumer peripheralConsumer;
    private PeripheralProvider provider;
    private boolean isDead = false;
    public TokenizedPeripheral(PeripheralConsumer peripheralConsumer, PeripheralProvider provider){
        this.peripheralConsumer = peripheralConsumer;
        this.provider = provider;
    }

    public boolean isDead(){
        if (isDead) return true;
        if (peripheralConsumer.isDead()) return true;
        return provider == null;
    }

    public Exposable getAPI(){
        if (isDead()) throw new RuntimeException("Peripheral is considered dead");
        return provider.getAPI(peripheralConsumer);
    }

    public void tick(short deltaTime){
        if (provider != null && provider.kill(peripheralConsumer)){
            provider = null;
            isDead = true;
        }
        if (!isDead()){
            provider.tick(peripheralConsumer, deltaTime);
        }
    }

    public UUID getProviderUuid(){
        return provider.getProviderUuid();
    }

    public boolean canBeDisabled(){
        return !provider.impermeable();
    }

    public String getName(){
        return provider.getType();
    }
}
