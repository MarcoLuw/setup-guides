package com.cns.ligo_trans.domain.message.service;

import com.cns.ligo_trans.global.config.SSLHelper;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TranslationService {

  private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);

  @Value("${spring.groq.api-key}")
  private String groqApiKey;

  @Value("${spring.groq.api-url}")
  private String groqApiUrl;

  private static final Map<String, String> LANGUAGE_MAP = Map.of(
      "ko", "Korean",
      "en", "English",
      "vi", "Vietnamese",
      "check_grammar:", "grammar checking",
      "ask_ligobot:", "ask ligobot"
  );

  public String translate(String text) throws NoSuchAlgorithmException, KeyManagementException {
    SSLHelper.disableSslVerification();

    String mode = LANGUAGE_MAP.keySet().stream()
        .filter(text::startsWith)
        .findFirst()
        .orElse(null);
    logger.info("mode: " + mode);

    if (mode == null) {
      logger.warn("Unknown translation mode: " + mode);
      return "Unsupported translation mode";
    }

    String contentToTranslate = text.substring(mode.length()).trim();
    String requestBody = buildRequestBody(mode, contentToTranslate);

    return sendTranslationRequest(requestBody);
  }

//  private String buildRequestBody(String mode, String content) {
//    return String.format(
//        "{ \"model\": \"llama-3.3-70b-versatile\", \"messages\": [{ \"role\": \"user\", \"content\": \"You are a helpful assistant that %s. The text is: %s\" }] }",
//        mode.equals("check_grammar:") ? "checks grammar for the text. You will only reply with the corrected text in the text language and nothing else." : "translates the text to " + LANGUAGE_MAP.get(mode) +". You will only reply with the translation text and nothing else.",
//        content
//    );
//  }

  private String buildRequestBody(String mode, String content) {
    String assistantRole;

    if (mode.equals("check_grammar:")) {
      assistantRole = "checks grammar for the text. You will only reply with the corrected text in the text language and nothing else.";
    } else if (mode.equals("ask_ligobot:")) {
      assistantRole = "answers for the following text.";
    } else {
      assistantRole = "translates the text to " + LANGUAGE_MAP.get(mode) + ". You will only reply with the translation text and nothing else.";
    }

    return String.format(
        "{ \"model\": \"llama-3.3-70b-versatile\", \"messages\": [{ \"role\": \"user\", \"content\": \"You are a helpful assistant that %s. The text is: %s\" }] }",
        assistantRole,
        content
    );
  }


  private String sendTranslationRequest(String requestBody) {
    try {
      RestTemplate restTemplate = new RestTemplate();
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", groqApiKey);
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
      ResponseEntity<String> response = restTemplate.exchange(groqApiUrl, HttpMethod.POST, entity, String.class);

      return extractTranslation(response.getBody());
    } catch (Exception e) {
      logger.error("Translation API error", e);
      return "An error occurred: " + e.getMessage();
    }
  }

  private String extractTranslation(String responseBody) {
    JSONObject jsonResponse = new JSONObject(responseBody);
    return jsonResponse.getJSONArray("choices")
        .getJSONObject(0)
        .getJSONObject("message")
        .getString("content");
  }
}
