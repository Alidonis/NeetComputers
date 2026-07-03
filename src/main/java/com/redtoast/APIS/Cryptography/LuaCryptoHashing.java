package com.redtoast.APIS.Cryptography;

import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.Exposable;
import com.redtoast.simulation.base.ExposedError;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LuaCryptoHashing implements Exposable {
    @Exposed
    public String SHA256(String data) {
        return digest(data, "SHA-256");
    }
    @Exposed
    public String MD5(String data) {
        return digest(data, "MD5");
    }
    String digest(String data, String digestAlgorithm) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(digestAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new ExposedError(e.getMessage());
        }
        byte[] encodedhash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        return LuaCryptoUtils.bytesToHex(encodedhash);
    }
}
