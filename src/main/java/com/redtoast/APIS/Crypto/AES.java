package com.redtoast.APIS.Crypto;

import com.redtoast.Computer;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.Exposable;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class AES implements Exposable {
    Computer computer;
    Runtime vm;
    static final String algorithm = "AES/GCM/NoPadding";

    public AES(Computer parent) throws NoSuchAlgorithmException {
        computer = parent;
        vm = computer.getRuntime();
    }
    @Exposed
    public static String generateIv() {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

    @Exposed
    public String GenerateKey() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        keyGenerator.init(256);
        SecretKey key = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
    @Exposed
    public String Encrypt(String SecretKey, String IV, String data) {
        Cipher cipher;
        SecretKey key;
        SecretKeyFactory keyFactory;
        try {
            keyFactory = SecretKeyFactory.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        try {
            key = keyFactory.generateSecret(new SecretKeySpec(Base64.getDecoder().decode(SecretKey), "AES"));
        } catch (InvalidKeySpecException e) {
            return null;
        }

        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            return null;
        }
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, Base64.getDecoder().decode(IV)));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            return null;
        }
        byte[] cipherText;
        try {
            cipherText = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
        return Base64.getEncoder().encodeToString(cipherText);
    }
    @Exposed
    public String Decrypt(String SecretKey, String IV, String data) {
        Cipher cipher;
        SecretKey key;
        SecretKeyFactory keyFactory;
        try {
            keyFactory = SecretKeyFactory.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        try {
            key = keyFactory.generateSecret(new SecretKeySpec(Base64.getDecoder().decode(SecretKey), "AES"));
        } catch (InvalidKeySpecException e) {
            return null;
        }

        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            return null;
        }
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Base64.getDecoder().decode(IV)));
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            return null;
        }
        byte[] cipherText;
        try {
            cipherText = cipher.doFinal(Base64.getDecoder().decode(data));
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
        return new String(cipherText, StandardCharsets.UTF_8);
    }
}