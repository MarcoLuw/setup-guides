package com.cns.ligo.domain.message.broker;

import com.cns.ligo.domain.message.model.Message;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class Sender {

  private final KafkaTemplate<String, Message> kafkaTemplate;

  public Sender(KafkaTemplate<String, Message> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void send(String topic, Message message) {
    kafkaTemplate.send(topic, message);
  }
}
