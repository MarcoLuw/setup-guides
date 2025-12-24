package com.example.mskdemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer Service
 * Handles publishing messages to Kafka topics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.name:demo-topic}")
    private String topicName;

    /**
     * Publishes a message to the configured Kafka topic.
     *
     * @param message The message to publish
     * @return CompletableFuture with the send result
     */
    public CompletableFuture<SendResult<String, String>> sendMessage(String message) {
        log.info("Sending message to topic '{}': {}", topicName, message);

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message sent successfully to partition {} with offset {}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send message: {}", ex.getMessage(), ex);
            }
        });

        return future;
    }

    /**
     * Publishes a message with a specific key to the configured Kafka topic.
     *
     * @param key     The message key (used for partitioning)
     * @param message The message to publish
     * @return CompletableFuture with the send result
     */
    public CompletableFuture<SendResult<String, String>> sendMessage(String key, String message) {
        log.info("Sending message with key '{}' to topic '{}': {}", key, topicName, message);

        return kafkaTemplate.send(topicName, key, message);
    }
}
