package com.cns.ligo_trans.domain.message.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {

  private MessageType type;
  private String content;
  private String sender;
  private String sessionId;

  private String translationMode;

}
