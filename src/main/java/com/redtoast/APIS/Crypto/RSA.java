package com.redtoast.APIS.Crypto;

import com.redtoast.Computer;
import com.redtoast.simulation.Runtime;
import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.Exposable;
import com.redtoast.simulation.value.Value;
import com.redtoast.simulation.value.ValueTypes.Tuple;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSA implements Exposable {
    Computer computer;
    Runtime vm;

    KeyFactory keyFactoryRSA;

    public RSA(Computer parent) throws NoSuchAlgorithmException {
        computer = parent;
        vm = computer.getRuntime();

        keyFactoryRSA = KeyFactory.getInstance("RSA");
    }

    @Exposed
    public Tuple GenerateKeyPair() {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        Key publicKey = keyPair.getPublic();
        Key privateKey = keyPair.getPrivate();
        byte[] KeyDataPublic = publicKey.getEncoded();
        byte[] KeyDataPrivate = privateKey.getEncoded();


        Tuple result = new Tuple();
        result.add(Value.of(Base64.getEncoder().encodeToString(KeyDataPublic)));
        result.add(Value.of(Base64.getEncoder().encodeToString(KeyDataPrivate)));
        return result;
    }
    @Exposed
    public String Encrypt(String PublicKey, String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] publicKeyBytes = Base64.getDecoder().decode(PublicKey);
        Cipher rsaCipher;
        try {
            rsaCipher = Cipher.getInstance("RSA");
        } catch (Exception e) {
            return null;
        }
        Key publicKeyInstance;
        try {
            publicKeyInstance = keyFactoryRSA.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (InvalidKeySpecException e) {
            return null;
        }

        try {
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKeyInstance);
        } catch (InvalidKeyException e) {
            return null;
        }
        byte[] dataEncrypted;

        try {
            dataEncrypted = rsaCipher.doFinal(dataBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }

        return Base64.getEncoder().encodeToString(dataEncrypted);
    }
    @Exposed
    public String Decrypt(String PrivateKey, String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] privateKeyBytes = Base64.getDecoder().decode(PrivateKey);
        Cipher rsaCipher;
        try {
            rsaCipher = Cipher.getInstance("RSA");
        } catch (Exception e) {
            return null;
        }
        Key privateKeyInstance;
        try {
            privateKeyInstance = keyFactoryRSA.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (InvalidKeySpecException e) {
            return null;
        }

        try {
            rsaCipher.init(Cipher.ENCRYPT_MODE, privateKeyInstance);
        } catch (InvalidKeyException e) {
            return null;
        }
        byte[] dataDecrypted;

        try {
            dataDecrypted = rsaCipher.doFinal(dataBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }

        return Base64.getEncoder().encodeToString(dataDecrypted);
    }
}