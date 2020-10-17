package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ChatMessage implements Serializable {
  private String sessionId;
  private UserHash userHash;
  private String message;
  private Date createdDate;
}
