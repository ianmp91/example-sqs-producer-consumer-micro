package com.example.sqsmicro.util;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class EncryptionMessageUtil {

    private PublicKey publicKey;

    public EncryptionMessageUtil() {
    }

    public EncryptionMessageUtil(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    // Movemos la carga del archivo a un @PostConstruct
    // Esto evita que falle la instanciación simple si el archivo no está,
    // y es más "Spring-way" para inicializar recursos.
    @PostConstruct
    private void init() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Solo cargamos si no se ha inyectado ya (por si acaso)
        if (this.publicKey == null) {
            loadPublicKey();
        }
    }

    private void loadPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String keyContent = Files.readString(Path.of("src/main/resources/public_key.pem"))
                .replaceAll("\\n", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "");

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(keyContent));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = keyFactory.generatePublic(keySpec);
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
}
