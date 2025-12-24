package com.example.sqsmicro.services;

import com.example.sqslib.iata.IATAAIDXFlightLegRQ;
import com.example.sqslib.service.XmlService;
import com.example.sqsmicro.records.MessageDto;
import com.example.sqslib.producer.SqsProducerService;
import com.example.sqsmicro.util.EncryptDecryptMessageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ian.paris
 * @since 2025-12-15
 */
@Slf4j
@Service
public class MessageProducerService {

    private final SqsProducerService sqsProducerService;
    private final EncryptDecryptMessageUtil encryptDecryptMessageUtil;
    private final String colaAwsSqsProducer;
    private final XmlService xmlService;

    public MessageProducerService(
            @Value("${cola.aws.sqs.producer}") String colaAwsSqsProducer,
            SqsProducerService sqsProducerService,
            EncryptDecryptMessageUtil encryptDecryptMessageUtil,
            XmlService xmlService) {
        this.colaAwsSqsProducer = colaAwsSqsProducer;
        this.sqsProducerService = sqsProducerService;
        this.encryptDecryptMessageUtil = encryptDecryptMessageUtil;
        this.xmlService = xmlService;
    }

    public void sendFlightLegRequest() throws Exception {
        IATAAIDXFlightLegRQ request = new IATAAIDXFlightLegRQ();
        request.setEchoToken(UUID.randomUUID().toString());
        request.setTimeStamp(LocalDateTime.now());
        request.setTarget("test");
        request.setVersion(new BigDecimal("21.3"));
        request.setTransactionIdentifier("t1");
        request.setSequenceNmbr(new BigInteger("1"));

        IATAAIDXFlightLegRQ.Airline airline = new IATAAIDXFlightLegRQ.Airline();
        airline.setCode("QR");
        airline.setCodeContext("IATA");

        request.setAirline(airline);

        String xmlPayload = xmlService.toXml(request);

        log.debug("Before preparing the SQS shipment. Payload to encrypt: {}", xmlPayload);

        EncryptDecryptMessageUtil.EncryptedMessageBundle encryptedMessageBundle = encryptDecryptMessageUtil.encryptHybrid(xmlPayload);

        MessageDto message = new MessageDto(
                Map.of("keyPublic", encryptDecryptMessageUtil.getPublicKeyAsString()),
                encryptedMessageBundle.encryptedPayload(),
                encryptedMessageBundle.encryptedKey()
        );

        log.info("Preparing the SQS shipment.  EncryptedPayload: {}", encryptedMessageBundle.encryptedPayload());

        sqsProducerService.send(colaAwsSqsProducer, message);

    }

    public void sendMessage(String payload, Map<String, String> metadata) throws Exception {
        log.debug("Before preparing the SQS shipment. Payload to encrypt: {}", payload);
        EncryptDecryptMessageUtil.EncryptedMessageBundle encryptedMessageBundle = encryptDecryptMessageUtil.encryptHybrid(payload);
        metadata.put("keyPublic", encryptDecryptMessageUtil.getPublicKeyAsString());
        MessageDto message = new MessageDto(
                metadata,
                encryptedMessageBundle.encryptedPayload(),
                encryptedMessageBundle.encryptedKey()
        );
        log.info("Preparing the SQS shipment. Metadata: {} | EncryptedPayload: {}", metadata.toString(), encryptedMessageBundle.encryptedPayload());
        sqsProducerService.send(colaAwsSqsProducer, message);
    }
}