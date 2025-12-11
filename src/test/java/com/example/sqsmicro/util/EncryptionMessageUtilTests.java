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

@SpringBootTest
public class EncryptionMessageUtilTests {

    @Autowired
    private EncryptionMessageUtil encryptionMessageUtil;

    // Guardamos las claves generadas para verificar la desencriptación
    private PrivateKey testPrivateKey;
    private PublicKey testPublicKey;

    @BeforeEach
    void setup() throws Exception {
        // 1. Generamos un par de claves RSA frescas para este test
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        this.testPrivateKey = pair.getPrivate();
        this.testPublicKey = pair.getPublic();

        // 2. MAGIA AQUÍ: Inyectamos la clave pública en el Bean, saltándonos el método loadPublicKey()
        // Esto simula que el método loadPublicKey() leyó el archivo correctamente.
        // "publicKey" debe coincidir con el nombre de la variable privada en tu clase.
        ReflectionTestUtils.setField(encryptionMessageUtil, "publicKey", testPublicKey);
    }

    @Test
    @DisplayName("Debe retornar la clave pública en formato String correctamente")
    void testGetPublicKeyAsString() {
        // Act
        String pubKeyString = encryptionMessageUtil.getPublicKeyAsString();

        // Assert
        assertNotNull(pubKeyString);
        // Validamos que lo que devuelve contiene parte de la codificación estándar
        assertTrue(pubKeyString.contains("MII") || pubKeyString.length() > 50,
                "Debe retornar una representación válida de la clave");
    }

    @Test
    @DisplayName("Debe encriptar el payload de forma que sea desencriptable con la clave privada")
    void testEncryptPayload() throws Exception {
        // 1. GIVEN
        String rawPayload = "{\"user_id\": 12345, \"status\": \"ACTIVE\"}";
        PublicKey publicKey = testPublicKey;
        EncryptionMessageUtil encryptionMessageUtil = new EncryptionMessageUtil(publicKey);
        // 2. WHEN
        // El método usa internamente la clave que inyectamos en el setup()
        String encryptedBase64 = encryptionMessageUtil.encryptPayload(rawPayload);

        // 3. THEN - Validaciones estructurales
        assertNotNull(encryptedBase64);
        assertNotEquals(rawPayload, encryptedBase64);

        // 4. VERIFY - La prueba de fuego: Intentar desencriptar con la clave PRIVADA que tenemos
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, testPrivateKey);

        byte[] decryptedBytes = decryptCipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
        String decryptedPayload = new String(decryptedBytes, StandardCharsets.UTF_8);

        assertEquals(rawPayload, decryptedPayload,
                "El payload encriptado por el Util debe poderse recuperar con la clave privada par");
    }
}
