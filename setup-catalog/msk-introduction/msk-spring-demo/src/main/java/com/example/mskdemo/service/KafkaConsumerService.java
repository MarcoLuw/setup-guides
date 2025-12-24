package com.example.mskdemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final ConsumerFactory<String, String> consumerFactory;

    @Value("${spring.kafka.topic.name:demo-topic}")
    private String topicName;

    @Value("${spring.kafka.consumer.group-id}")
    private String configuredGroupId;

    /**
     * Polls Kafka for available messages from the beginning.
     *
     * @param limit Maximum number of messages to return
     * @return List of messages with offset, partition, and value
     */
    public List<Map<String, Object>> consumeMessages(int limit) {
        List<Map<String, Object>> messages = new ArrayList<>();

        // Use the group-id from application.yml (no hardcoded strings)
        String groupId = configuredGroupId;

        try (Consumer<String, String> consumer = consumerFactory.createConsumer(groupId, null)) {

            consumer.subscribe(Collections.singletonList(topicName));

            // Wait for partition assignment (IAM MSK requires time)
            Set<TopicPartition> partitions = Collections.emptySet();
            int attempts = 0;

            while (partitions.isEmpty() && attempts < 15) {
                consumer.poll(Duration.ofMillis(300));
                partitions = consumer.assignment();
                attempts++;
            }

            if (partitions.isEmpty()) {
                log.warn("No partitions assigned for topic '{}'. IAM auth or network may be blocking connection.", topicName);
                return messages;
            }

            log.info("Assigned partitions for topic '{}': {}", topicName, partitions);

            // Seek to beginning for ALL partitions
            consumer.seekToBeginning(partitions);

            // Poll for messages
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(3));

            for (ConsumerRecord<String, String> record : records) {
                if (messages.size() >= limit) break;

                Map<String, Object> msg = new LinkedHashMap<>();
                msg.put("offset", record.offset());
                msg.put("partition", record.partition());
                msg.put("value", record.value());
                messages.add(msg);
            }

            log.info("Successfully polled {} messages from '{}'", messages.size(), topicName);

        } catch (Exception e) {
            log.error("Error polling messages: {}", e.toString(), e);
        }

        return messages;
    }
}
