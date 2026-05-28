package com.redtoast.APIS.Crypto;

import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.Exposable;

import java.security.SecureRandom;

public class LuaCryptoSecureRNG implements Exposable {
    @Exposed
    public double GetRandomInt() {
        return new SecureRandom().nextDouble(Double.MIN_VALUE, Double.MAX_VALUE);
    }
    @Exposed
    public double GetRandomIntFromMin(double min) {
        return new SecureRandom().nextDouble(min, Double.MAX_VALUE);
    }
    @Exposed
    public double GetRandomIntUpTo(double max) {
        if (max == Double.MAX_VALUE)
            return new SecureRandom().nextDouble(Double.MIN_VALUE, max);
        return new SecureRandom().nextDouble(Double.MIN_VALUE, max + 1);
    }
    @Exposed
    public double GetRandomBetween(double min, double max) {
        if (max == Double.MAX_VALUE)
            return new SecureRandom().nextDouble(min, max);
        return new SecureRandom().nextDouble(min, max + 1);
    }
}
