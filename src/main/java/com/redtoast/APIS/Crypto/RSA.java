package com.redtoast.APIS.Crypto;

import com.redtoast.simulation.annotations.Exposed;
import com.redtoast.simulation.base.Exposable;
import com.redtoast.simulation.base.ExposedError;
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
    KeyFactory keyFactoryRSA;

    public RSA() throws NoSuchAlgorithmException {
        keyFactoryRSA = KeyFactory.getInstance("RSA");
    }

    @Exposed
    public Tuple GenerateKeyPair() {
        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new ExposedError(e.getMessage());
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
            throw new ExposedError(e.getMessage());
        }
        Key publicKeyInstance;
        try {
            publicKeyInstance = keyFactoryRSA.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (InvalidKeySpecException e) {
            throw new ExposedError(e.getMessage());
        }

        try {
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKeyInstance);
        } catch (InvalidKeyException e) {
            throw new ExposedError(e.getMessage());
        }
        byte[] dataEncrypted;

        try {
            dataEncrypted = rsaCipher.doFinal(dataBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new ExposedError(e.getMessage());
        }

        return Base64.getEncoder().encodeToString(dataEncrypted);
    }
    @Exposed
    public String Decrypt(String PrivateKey, String data) {
        byte[] dataBytes = Base64.getDecoder().decode(data);
        byte[] privateKeyBytes = Base64.getDecoder().decode(PrivateKey);
        Cipher rsaCipher;
        try {
            rsaCipher = Cipher.getInstance("RSA");
        } catch (Exception e) {
            throw new ExposedError(e.getMessage());
        }
        Key privateKeyInstance;
        try {
            privateKeyInstance = keyFactoryRSA.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (InvalidKeySpecException e) {
            throw new ExposedError(e.getMessage());
        }

        try {
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKeyInstance);
        } catch (InvalidKeyException e) {
            throw new ExposedError(e.getMessage());
        }
        byte[] dataDecrypted;

        try {
            dataDecrypted = rsaCipher.doFinal(dataBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new ExposedError(e.getMessage());
        }

        return Base64.getEncoder().encodeToString(dataDecrypted);
    }
    @Exposed
    public String Sign(String PrivateKey, String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] privateKeyBytes = Base64.getDecoder().decode(PrivateKey);
        Signature rsaSigner;
        try {
            rsaSigner = Signature.getInstance("SHA256withRSA");
        } catch (Exception e) {
            throw new ExposedError(e.getMessage());
        }
        PrivateKey privateKeyInstance;
        try {
            privateKeyInstance = keyFactoryRSA.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (InvalidKeySpecException e) {
            throw new ExposedError(e.getMessage());
        }
        try {
            rsaSigner.initSign(privateKeyInstance);
        } catch (InvalidKeyException e) {
            throw new ExposedError(e.getMessage());
        }
        try {
            rsaSigner.update(dataBytes);
        } catch (SignatureException e) {
            throw new ExposedError(e.getMessage());
        }
        try {
            return Base64.getEncoder().encodeToString(rsaSigner.sign());
        } catch (SignatureException e) {
            throw new ExposedError(e.getMessage());
        }
    }
    @Exposed
    public boolean Verify(String PublicKey, String data, String signature) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] publicKeyBytes = Base64.getDecoder().decode(PublicKey);
        Signature rsaSigner;
        try {
            rsaSigner = Signature.getInstance("SHA256withRSA");
        } catch (Exception e) {
            return false;
        }
        PublicKey publicKeyInstance;
        try {
            publicKeyInstance = keyFactoryRSA.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (InvalidKeySpecException e) {
            return false;
        }
        try {
            rsaSigner.initVerify(publicKeyInstance);
        } catch (InvalidKeyException e) {
            return false;
        }
        try {
            rsaSigner.update(dataBytes);
        } catch (SignatureException e) {
            return false;
        }
        try {
            return rsaSigner.verify(Base64.getDecoder().decode(signature));
        } catch (SignatureException e) {
            return false;
        }
    }
}