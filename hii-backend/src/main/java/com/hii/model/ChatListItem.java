package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ChatListItem {
  private String name;
  private String sessionId;
  private String lastMessage;
  private int unread;
  private boolean groupChat;
  private Date updatedDate;
}
