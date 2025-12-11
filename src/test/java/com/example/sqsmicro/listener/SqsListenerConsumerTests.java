package com.example.sqsmicro.listener;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class SqsListenerConsumerTests {

    @Autowired
    private SqsTemplate sqsTemplate;

    // ESPIAMOS el Listener real.
    // Esto permite que el método listenResponse se ejecute de verdad,
    // pero nos deja preguntar "¿se ejecutó?".
    @MockitoSpyBean
    private SqsListenerConsumer sqsListenerConsumer;

    @Test
    void testReceiveMessageFromQueue2() {
        // GIVEN
        String payload = "Probando listener simple";

        // WHEN
        // Enviamos el mensaje a la cola que escucha el listener
        sqsTemplate.send(to -> to.queue("cola-aws-sqs-2").payload(payload));

        // THEN
        // Verificamos que el framework de Spring detectó el mensaje
        // y llamó a TU método listenResponse dentro del Bean espiado.
        // Usamos timeout() porque SQS es asíncrono.
        verify(sqsListenerConsumer, timeout(3000).times(1))
                .listenResponse(eq(payload)); // O usa anyString() si el payload cambia
    }
}
