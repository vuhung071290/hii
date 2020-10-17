package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ChatMessageResponse extends WsMessage implements Serializable {
  private String sessionId;
  private String userId;
  private String name;
  private String message;
  private Date createdDate;
}
