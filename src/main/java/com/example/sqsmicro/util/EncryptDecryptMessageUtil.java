package com.example.sqsmicro.util;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * @author ian.paris
 * @since 2025-12-15
 */
@Component
public class EncryptDecryptMessageUtil {

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public EncryptDecryptMessageUtil(
            @Value("classpath:private_key_b.pem") Resource resourcePrivateKey,
            @Value("classpath:public_key_c.pem") Resource resourcePublicKey) throws Exception{
        if (!resourcePublicKey.exists() && !resourcePublicKey.exists()) {
            throw new RuntimeException("FATAL ERROR: private & public KEY not found in src/main/resource");
        }
        loadPublicKey(resourcePublicKey);
        loadPrivateKey(resourcePrivateKey);
    }

    private void loadPrivateKey(Resource resource) throws Exception {
        try (Reader reader = new InputStreamReader(resource.getInputStream());
             PEMParser pemParser = new PEMParser(reader)) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            if (object instanceof PEMKeyPair) {
                this.privateKey = converter.getKeyPair((PEMKeyPair) object).getPrivate();
            } else if (object instanceof PrivateKeyInfo) {
                this.privateKey = converter.getPrivateKey((PrivateKeyInfo) object);
            } else {
                throw new RuntimeException("The private_key_b.pem file does not have a supported format (expected PKCS#1 or PKCS#8)");
            }
        }
    }

    private void loadPublicKey(Resource resource) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        try (Reader reader = new InputStreamReader(resource.getInputStream());
             PEMParser pemParser = new PEMParser(reader)) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            if (object instanceof PEMKeyPair) {
                this.publicKey = converter.getKeyPair((PEMKeyPair) object).getPublic();
            } else if (object instanceof SubjectPublicKeyInfo) {
                this.publicKey = converter.getPublicKey((SubjectPublicKeyInfo) object);
            } else {
                throw new RuntimeException("The public_key_c.pem file does not have a supported format (expected PKCS#1 or PKCS#8)");
            }
        }
    }

    public String encryptPayload(String rawPayload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(rawPayload.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String getPublicKeyAsString()  {
        return Base64.getEncoder().encodeToString(this.publicKey.getEncoded());
    }

    // Retorna un objeto o un DTO simple con las dos partes
    public EncryptedMessageBundle encryptHybrid(String rawPayload) throws Exception {

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // AES-256
        SecretKey aesKey = keyGen.generateKey();

        Cipher aesCipher = Cipher.getInstance("AES"); // O "AES/ECB/PKCS5Padding"
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedXmlBytes = aesCipher.doFinal(rawPayload.getBytes());
        String encryptedPayloadBase64 = Base64.getEncoder().encodeToString(encryptedXmlBytes);

        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedKeyBytes = rsaCipher.doFinal(aesKey.getEncoded());
        String encryptedKeyBase64 = Base64.getEncoder().encodeToString(encryptedKeyBytes);

        return new EncryptedMessageBundle(encryptedPayloadBase64, encryptedKeyBase64);
    }

    public String decryptHybrid(String encryptedPayload, String encryptedAesKeyBase64) throws Exception {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(Base64.getDecoder().decode(encryptedAesKeyBase64));
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decryptedBytes = aesCipher.doFinal(Base64.getDecoder().decode(encryptedPayload));

        return new String(decryptedBytes);
    }

    public record EncryptedMessageBundle(String encryptedPayload, String encryptedKey) {}
}

