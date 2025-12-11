package com.example.sqsmicro.controllers;

import com.example.sqsmicro.services.MessageProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MessageProducerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Mockeamos el servicio real para aislar la prueba del Controller
    // O si prefieres probar la integración con el servicio, mockea la Libreria (A) dentro
    @MockitoBean
    private MessageProducerService messageProducerService;

    @Test
    void testSendMessage_ShouldEncryptAndDelegateToService() throws Exception {
        // GIVEN: Un payload de prueba
        Map<String, String> request = new HashMap<>();
        request.put("payload", "LAX 123");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("idLogi", "idLogi");
        // WHEN: Hacemos la llamada HTTP POST
        mockMvc.perform(post("/api/v1/messages/produce")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // O isCreated()

        // THEN: Verificamos que el Controller pasó los datos al servicio
        verify(messageProducerService).sendSecureMessage(request.get("payload"), metadata);
    }
}
