package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ChatItem implements Serializable {
  private String userId;
  private String message;
  private String name;
  private Date createdDate;
}
