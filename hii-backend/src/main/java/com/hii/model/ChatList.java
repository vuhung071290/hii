package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ChatList implements Serializable {
  private List<UserHash> userHashes;
  private String sessionId;
  private Date updatedDate;
  private String lastMessage;
}
