package com.redtoast.APIS.Crypto;

import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.Exposable;
import com.redtoast.simulation.value.ValueTypes.Table;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class LuaCryptoSecureRNG implements Exposable {
    @Exposed
    public int GetRandomInt() {
        return new SecureRandom().nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    @Exposed
    public int GetRandomIntFromMin(int min) {
        return new SecureRandom().nextInt(min, Integer.MAX_VALUE);
    }
    @Exposed
    public int GetRandomIntUpTo(int max) {
        return new SecureRandom().nextInt(Integer.MIN_VALUE, max);
    }
    @Exposed
    public int GetRandomBetween(int min, int max) {
        return new SecureRandom().nextInt(min, max);
    }
}
