//package com.cns.ligo.global.kafka;
//
//import com.cns.ligo.domain.message.model.Message;
//import java.util.HashMap;
//import java.util.Map;
//import org.springframework.beans.factory.annotation.Value;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//import org.springframework.kafka.support.serializer.JsonSerializer;
//
//@Configuration
//public class KafkaProducerConfig {
//  @Value("${spring.kafka.producer.bootstrap-servers}")
//  private String bootstrapAddress;
//
//  @Bean
//  public <K, V> ProducerFactory<K, V> producerFactory() {
//    Map<String, Object> configProps = new HashMap<>();
//    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
//    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
////    configProps.put(JsonSerializer.TYPE_MAPPINGS, "message:com.cns.ligo.domain.message.model.Message, message:com.cns.ligo_trans.domain.message.model.Message");
////    configProps.put(ProducerConfig.RETRIES_CONFIG, 10);
////    configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 5000);
////    configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10000);
//    return new DefaultKafkaProducerFactory<>(configProps);
//  }
//
//  @Bean
//  public <K, V> KafkaTemplate<K, V> kafkaTemplate() {
//    return new KafkaTemplate<>(producerFactory());
//  }
//}
