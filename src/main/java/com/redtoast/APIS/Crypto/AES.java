package com.redtoast.APIS.Crypto;

import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.Exposable;
import com.redtoast.simulation.base.ExposedError;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class AES implements Exposable {
    static final String algorithm = "AES/GCM/NoPadding";

    public AES() {

    }
    @Exposed
    public String GenerateIv() {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

    @Exposed
    public String GenerateKey() {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new ExposedError(e.getMessage());
        }
        keyGenerator.init(256);
        SecretKey key = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    @Exposed
    public String Encrypt(String SecretKey, String IV, String data) {
        Cipher cipher;
        SecretKey key;

        key = new SecretKeySpec(Base64.getDecoder().decode(SecretKey), "AES");

        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new ExposedError(e.getMessage());
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, Base64.getDecoder().decode(IV)));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new ExposedError(e.getMessage());
        }
        byte[] cipherText;
        try {
            cipherText = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new ExposedError(e.getMessage());
        }
        return Base64.getEncoder().encodeToString(cipherText);
    }
    @Exposed
    public String Decrypt(String SecretKey, String IV, String data) {
        Cipher cipher;
        SecretKey key;

        key = new SecretKeySpec(Base64.getDecoder().decode(SecretKey), "AES");

        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new ExposedError(e.getMessage());
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Base64.getDecoder().decode(IV)));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new ExposedError(e.getMessage());
        }
        byte[] cipherText;
        try {
            cipherText = cipher.doFinal(Base64.getDecoder().decode(data));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new ExposedError(e.getMessage());
        }
        return new String(cipherText, StandardCharsets.UTF_8);
    }
}