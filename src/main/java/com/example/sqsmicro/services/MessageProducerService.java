package com.example.sqsmicro.services;

import com.example.sqsmicro.records.MessageDto;
import com.example.sqslib.producer.SqsProducerService;
import com.example.sqsmicro.util.EncryptionMessageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class MessageProducerService {

    private final SqsProducerService sqsProducerService;
    private final EncryptionMessageUtil encryptionMessageUtil;
    private final String colaAwsSqsProducer;

    public MessageProducerService(
            @Value("${cola.aws.sqs.producer}") String colaAwsSqsProducer,
            SqsProducerService sqsProducerService, EncryptionMessageUtil encryptionMessageUtil) {
        this.colaAwsSqsProducer = colaAwsSqsProducer;
        this.sqsProducerService = sqsProducerService;
        this.encryptionMessageUtil = encryptionMessageUtil;
    }

    public void sendSecureMessage(String payload, Map<String, String> metadata) throws Exception {
        String encryptedPayload = encryptionMessageUtil.encryptPayload(payload);
        MessageDto message = new MessageDto(
                metadata,
                encryptedPayload,
                encryptionMessageUtil.getPublicKeyAsString()
        );
        sqsProducerService.send(colaAwsSqsProducer, message);
    }
}