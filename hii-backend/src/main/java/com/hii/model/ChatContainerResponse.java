package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatContainerResponse extends WsMessage {
  private boolean isChatGroup;
  private String sessionId;
  private List<ChatItem> chatItems;
}
