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

/**
 * @author ian.paris
 * @since 2025-12-15
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MessageProducerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private MessageProducerService messageProducerService;

    @Test
    void testSendMessage_ShouldEncryptAndDelegateToService() throws Exception {
        MessagePayloadDto messagePayloadDto = new MessagePayloadDto();
        messagePayloadDto.setPayload("LAX-123");
        messagePayloadDto.setMetadata(Map.of("idLogi", "logi123"));
        mockMvc.perform(post("/api/v1/producer/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messagePayloadDto)))
                .andExpect(status().isOk());
        verify(messageProducerService).sendMessage(messagePayloadDto.getPayload(), messagePayloadDto.getMetadata());
    }
}
