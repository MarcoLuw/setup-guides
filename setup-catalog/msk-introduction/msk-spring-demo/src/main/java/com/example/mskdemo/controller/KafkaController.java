package com.example.mskdemo.controller;

import com.example.mskdemo.service.KafkaConsumerService;
import com.example.mskdemo.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Kafka operations.
 * Exposes endpoints to publish messages to Kafka.
 */
@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
@Slf4j
public class KafkaController {

    private final KafkaProducerService producerService;
    private final KafkaConsumerService consumerService;

    /**
     * Publishes a message to Kafka.
     *
     * POST /api/kafka/publish
     * Body: { "message": "hello" }
     *
     * @param payload JSON body containing the message
     * @return Response indicating success or failure
     */
    @PostMapping("/publish")
    public ResponseEntity<Map<String, String>> publishMessage(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Message cannot be empty"));
        }

        log.info("Received publish request with message: {}", message);

        try {
            producerService.sendMessage(message);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Message sent to Kafka"
            ));
        } catch (Exception e) {
            log.error("Failed to publish message", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send message: " + e.getMessage()));
        }
    }

    /**
     * Consumes messages from Kafka.
     *
     * GET /api/kafka/consume?limit=10
     *
     * @param limit Maximum number of messages to return (default 10)
     * @return List of messages
     */
    @GetMapping("/consume")
    public ResponseEntity<Map<String, Object>> consumeMessages(
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Received consume request with limit: {}", limit);

        List<Map<String, Object>> messages = consumerService.consumeMessages(limit);

        return ResponseEntity.ok(Map.of(
                "count", messages.size(),
                "messages", messages
        ));
    }
}
