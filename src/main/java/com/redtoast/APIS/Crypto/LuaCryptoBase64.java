package com.redtoast.APIS.Crypto;

import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.Exposable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LuaCryptoBase64 implements Exposable {
    @Exposed
    public String Encode(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }
    @Exposed
    public String Decode(String data) {
        return new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
    }
}
