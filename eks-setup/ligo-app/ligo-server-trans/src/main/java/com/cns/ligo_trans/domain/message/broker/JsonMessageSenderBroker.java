package com.cns.ligo_trans.domain.message.broker;

import com.cns.ligo_trans.domain.message.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class JsonMessageSenderBroker {
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public JsonMessageSenderBroker(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
  }

  public void send(String topic, Message message) throws JsonProcessingException {
    String jsonMessage = objectMapper.writeValueAsString(message);
    kafkaTemplate.send(topic, jsonMessage);
  }

}
