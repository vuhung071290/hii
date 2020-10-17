package com.hii.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatContainerRequest extends WsMessage {
  private String sessionId;
}
