package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class NewChatSessionResponse extends WsMessage implements Serializable {
  private String sessionId;
}
