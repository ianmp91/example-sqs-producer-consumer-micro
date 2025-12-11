package com.example.sqsmicro.controllers;

import com.example.sqsmicro.services.MessageProducerService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageProducerController {

    private final MessageProducerService messageProducerService;

    public MessageProducerController(MessageProducerService messageProducerService) {
        this.messageProducerService = messageProducerService;
    }

    @PostMapping("/produce")
    public String handleMessageProduce(@RequestBody Map<String, String> request) throws Exception {
        String payload = request.get("payload");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("idLogi", "idLogi");
        messageProducerService.sendSecureMessage(payload, metadata);

        return "Mensaje encriptado enviado a Cola SQS 1.";
    }
}
