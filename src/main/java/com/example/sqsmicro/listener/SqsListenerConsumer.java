package com.example.sqsmicro.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class SqsListenerConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsListenerConsumer.class);

    @SqsListener("${cola.aws.sqs.consumer}")
    public void listenResponse(@Payload String messagePayload) {
        log.info("Mensaje recibido desde cola-aws-sqs-2: {}", messagePayload);

    }


}
