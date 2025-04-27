package com.cns.ligo_trans.domain.message.controller;

import com.cns.ligo_trans.domain.message.service.TranslationService;
import com.cns.ligo_trans.domain.message.model.Message;
import com.cns.ligo_trans.domain.message.broker.JsonMessageSenderBroker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MessageController {

  private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

  private final TranslationService translationService;
  private final JsonMessageSenderBroker jsonMessageSenderBroker;

  public MessageController(TranslationService translationService,
      JsonMessageSenderBroker jsonMessageSenderBroker) {
    this.translationService = translationService;
    this.jsonMessageSenderBroker = jsonMessageSenderBroker;
  }

  @KafkaListener(topics = "translate-request", groupId = "chat-group")
  public void consumeTranslateRequest(String jsonMessage) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Message chatMessage = objectMapper.readValue(jsonMessage, Message.class);

    switch (chatMessage.getTranslationMode()) {
      case "ko":
        chatMessage.setContent("ko"+chatMessage.getContent());
        logger.info("send message ko");
        break;
      case "en":
        chatMessage.setContent("en"+chatMessage.getContent());
        break;
      case "vi":
        chatMessage.setContent("vi"+chatMessage.getContent());
        break;
//      case "ko-vi":
//        chatMessage.setContent("ko-vi:"+chatMessage.getContent());
//        break;
      default:
        logger.warn("Unknown translation mode: " + chatMessage.getTranslationMode());
    }

    String translatedText = translationService.translate(chatMessage.getContent());
    chatMessage.setContent(chatMessage.getTranslationMode() +": "+ translatedText);
    jsonMessageSenderBroker.send("ai-response", chatMessage);
  }

  @KafkaListener(topics = "check-grammar", groupId = "chat-group")
  public void consumeCheckGrammarRequest(String jsonMessage) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Message message = objectMapper.readValue(jsonMessage, Message.class);

    String grammarCheckedText = translationService.translate("check_grammar:" + message.getContent());
    message.setContent(grammarCheckedText);

    // send checked message to kafka
    jsonMessageSenderBroker.send("check-grammar-response", message);
  }

  @KafkaListener(topics = "ask-ligobot", groupId = "chat-group")
  public void consumeAskLigoBotRequest(String jsonMessage) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    Message message = objectMapper.readValue(jsonMessage, Message.class);

    String askLigoBotText = translationService.translate("ask_ligobot:" + message.getContent());
    message.setContent(askLigoBotText);

    // send checked message to kafka
    jsonMessageSenderBroker.send("ask-ligobot-response", message);
  }

}
