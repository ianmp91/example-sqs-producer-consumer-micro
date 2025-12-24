package com.example.sqsmicro.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author ian.paris
 * @since 2025-12-15
 */
@SpringBootTest
public class EncryptDecryptMessageUtilTests {

    @Autowired
    private EncryptDecryptMessageUtil encryptDecryptMessageUtil;

    private PrivateKey testPrivateKey;
    private PublicKey testPublicKey;

    @BeforeEach
    void setup() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        this.testPrivateKey = pair.getPrivate();
        this.testPublicKey = pair.getPublic();
        ReflectionTestUtils.setField(encryptDecryptMessageUtil, "publicKey", testPublicKey);
    }

    @Test
    @DisplayName("Debe retornar la clave pública en formato String correctamente")
    void testGetPublicKeyAsString() {
        // Act
        String pubKeyString = encryptDecryptMessageUtil.getPublicKeyAsString();
        // Assert
        assertNotNull(pubKeyString);
        // We validate that what it returns contains part of the standard encoding
        assertTrue(pubKeyString.contains("MII") || pubKeyString.length() > 50,
                "Debe retornar una representación válida de la clave");
    }

    @Test
    @DisplayName("Debe encriptar el payload de forma que sea desencriptable con la clave privada")
    void testEncryptPayload() throws Exception {
        // 1. GIVEN
        String rawPayload = "{\"user_id\": 12345, \"status\": \"ACTIVE\"}";
        PublicKey publicKey = testPublicKey;
        // 2. WHEN
        String encryptedBase64 = encryptDecryptMessageUtil.encryptPayload(rawPayload);
        // 3. THEN
        assertNotNull(encryptedBase64);
        assertNotEquals(rawPayload, encryptedBase64);
        // 4. VERIFY -
        // The acid test: Trying to decrypt with the private key we have
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, testPrivateKey);
        byte[] decryptedBytes = decryptCipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
        String decryptedPayload = new String(decryptedBytes, StandardCharsets.UTF_8);
        assertEquals(rawPayload, decryptedPayload,
                "The payload encrypted by the Util should be recoverable with the private key pair");
    }
}
