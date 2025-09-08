package com.redtoast.external;

import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.base.API;

public class TokenizedPeripheral {
    public final Runtime runtime;
    private PeripheralProvider provider;
    private boolean isDead = false;
    public TokenizedPeripheral(Runtime runtime, PeripheralProvider provider){
        this.runtime = runtime;
        this.provider = provider;
    }

    public boolean isDead(){
        if (isDead) return true;
        if (runtime.isDead()) return true;
        return provider == null;
    }

    public API getAPI(){
        if (isDead()) throw new RuntimeException("Peripheral is considered dead");
        return provider.getAPI(runtime);
    }

    public void tick(){
        if (provider != null && provider.kill(runtime)){
            provider = null;
            isDead = true;
        }
        if (!isDead()){
            provider.tick(runtime);
        }
    }
}
