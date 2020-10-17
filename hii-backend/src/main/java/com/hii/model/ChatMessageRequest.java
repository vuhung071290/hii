package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ChatMessageRequest extends WsMessage implements Serializable {
  private String message;
  private String sessionId;
  private List<String> usernames;
  private boolean groupChat;
}
