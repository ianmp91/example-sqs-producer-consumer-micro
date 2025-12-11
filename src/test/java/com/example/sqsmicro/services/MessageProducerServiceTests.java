package com.example.sqsmicro.services;

import com.example.sqslib.producer.SqsProducerService;
import com.example.sqsmicro.records.MessageDto;
import com.example.sqsmicro.util.EncryptionMessageUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageProducerServiceTests {

    @Mock
    private SqsProducerService sqsProducerLib; // Mock de tu Libreria (A)

    @Mock
    private EncryptionMessageUtil encryptionMessageUtil; // Mock de encriptación

    private MessageProducerService messageProducerService; // Servicio (B) bajo test

    @Test
    void testSendEncryptedRequest_ShouldEncryptAndSendToQueue1() throws Exception {
        // GIVEN
        String rawPayload = "LAX 123";
        Map<String,String> metadata = new HashMap<>();
        String publicKey = "public-key-123";
        metadata.put("publicKey", publicKey);

        // 1. Stub para encriptar el payload (tu servicio ahora devuelve String)
        when(encryptionMessageUtil.encryptPayload(eq(rawPayload)))
                .thenReturn("payload-encriptado-base64-falso");

        // 2. Stub para la Public Key
        String dummyPublicKey = "clave-publica-falsa";
        when(encryptionMessageUtil.getPublicKeyAsString())
                .thenReturn(dummyPublicKey);

        // Lo más seguro y limpio es instanciarlo tú:
        messageProducerService = new MessageProducerService(
                "cola-aws-sqs-1", // Pasas el valor directamente
                sqsProducerLib,        // Tu Mock
                encryptionMessageUtil     // Tu Mock
        );

        // WHEN
        messageProducerService.sendSecureMessage(rawPayload, metadata);

        // THEN
        verify(encryptionMessageUtil).encryptPayload(rawPayload);
        // Verificamos que se llamó al envío con los datos transformados
        verify(sqsProducerLib).send(eq("cola-aws-sqs-1"), any(MessageDto.class));
    }
}
