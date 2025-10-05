package com.cns.ligo.domain.message.controller;

import com.cns.ligo.domain.message.model.Message;
import com.cns.ligo.domain.message.model.MessageType;
import com.cns.ligo.domain.message.model.MessageEntity;
import com.cns.ligo.domain.message.broker.JsonMessageSenderBroker;
import com.cns.ligo.domain.message.broker.Sender;
import com.cns.ligo.domain.message.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RestController
public class MessageController {

  private final Sender sender;
  private final SimpMessageSendingOperations messagingTemplate;
  private final JsonMessageSenderBroker jsonMessageSenderBroker;
  private final MessageService messageService;

  private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

  public MessageController(Sender sender, SimpMessageSendingOperations messagingTemplate,
      JsonMessageSenderBroker jsonMessageSenderBroker, MessageService messageService) {
    this.sender = sender;
    this.messagingTemplate = messagingTemplate;
    this.jsonMessageSenderBroker = jsonMessageSenderBroker;
    this.messageService = messageService;
  }

  // noti to all user when someone connects
  @MessageMapping("/chat.add-user")
  @SendTo("/topic/public")
  public Message addUser(
      @Payload Message chatMessage,
      SimpMessageHeaderAccessor headerAccessor
  ) {
    if (headerAccessor.getSessionAttributes() != null) {
      headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
    }
    return chatMessage;
  }

  // REST endpoint to get recent messages
  @GetMapping("/api/messages/recent")
  public ResponseEntity<List<Message>> getRecentMessages() {
    try {
      List<MessageEntity> entities = messageService.getRecentMessages(10);
      List<Message> messages = entities.stream()
          .map(entity -> Message.builder()
              .sender(entity.getSender())
              .content(entity.getContent())
              .type(MessageType.valueOf(entity.getMessageType()))
              .build())
          .collect(Collectors.toList());
      logger.info("Retrieved {} recent messages from database", messages.size());
      return ResponseEntity.ok(messages);
    } catch (Exception e) {
      logger.error("Failed to retrieve recent messages", e);
      return ResponseEntity.ok(List.of());
    }
  }

  // send message
  @MessageMapping("/chat.send-message")
  public void sendMessage(@Payload Message chatMessage, SimpMessageHeaderAccessor headerAccessor)
      throws JsonProcessingException {
    chatMessage.setSessionId(headerAccessor.getSessionId());
    sender.send("messaging", chatMessage); // to kafka
    logger.info("Sending message: " + chatMessage);

    if (!chatMessage.getTranslationMode().equals("none")) {
      jsonMessageSenderBroker.send("translate-request", chatMessage);
    }
  }

  // Check-Grammar
  @MessageMapping("/chat.check-grammar")
  @SendTo("/topic/public")
  public void checkGrammar(@Payload Message chatMessage,
      SimpMessageHeaderAccessor headerAccessor) throws JsonProcessingException {
    chatMessage.setSessionId(headerAccessor.getSessionId());
    jsonMessageSenderBroker.send("check-grammar", chatMessage);
    logger.info("Sending check-grammer message: " + chatMessage);
  }

  // Ask LigoBot
  @MessageMapping("/chat.ask-ligobot")
  @SendTo("/topic/public")
  public void askLigoBot(@Payload Message chatMessage,
      SimpMessageHeaderAccessor headerAccessor) throws JsonProcessingException {
    chatMessage.setSessionId(headerAccessor.getSessionId());
    jsonMessageSenderBroker.send("ask-ligobot", chatMessage);
    logger.info("Sending ask-ligobot message: " + chatMessage);
  }

  // send chat message to all users
  @KafkaListener(topics = "messaging", groupId = "chat")
  public void consume(Message chatMessage) {
    logger.info("Received message from Kafka: " + chatMessage);
    
    // Persist message to database
    try {
      messageService.saveMessage(
          chatMessage.getSender(),
          chatMessage.getContent(),
          chatMessage.getType() != null ? chatMessage.getType().toString() : "CHAT",
          "default"
      );
      logger.info("Message persisted to database");
    } catch (Exception e) {
      logger.error("Failed to persist message to database", e);
    }
    
    messagingTemplate.convertAndSend("/topic/public", chatMessage);
  }

  // send ai-response message to all user
  @KafkaListener(topics = "ai-response", groupId = "chat")
  public void consumeAiResponse(String jsonMessage) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Message message = objectMapper.readValue(jsonMessage, Message.class);
    messagingTemplate.convertAndSend("/topic/public", message);
  }

  // send checked-grammar response to sender
  @KafkaListener(topics = "check-grammar-response", groupId = "chat")
  public void consumeCheckGrammarResponse(String jsonMessage) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Message message = objectMapper.readValue(jsonMessage, Message.class);
    logger.info("Received check-grammar message from Kafka-consumer: " + message.getSender());
    message.setType(MessageType.GRAMMAR_RESULT);
    messagingTemplate.convertAndSendToUser(message.getSessionId(), "/queue/private/checkgrammar", message,
        createHeaders(message.getSessionId()));
  }

  // send ask-ligo-bot response to sender
  @KafkaListener(topics = "ask-ligobot-response", groupId = "chat")
  public void consumeAskLigoBotResponse(String jsonMessage) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    Message message = objectMapper.readValue(jsonMessage, Message.class);
    logger.info("Received ask-ligobot message from Kafka-consumer: " + message.getSender());
    message.setType(MessageType.LIGOBOT_RESULT);
    messagingTemplate.convertAndSendToUser(message.getSessionId(), "/queue/private/askligobot", message,
        createHeaders(message.getSessionId()));
  }

  private MessageHeaders createHeaders(String sessionId) {
    SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor
        .create(SimpMessageType.MESSAGE);
    headerAccessor.setSessionId(sessionId);
    headerAccessor.setLeaveMutable(true);
    return headerAccessor.getMessageHeaders();
  }
}
