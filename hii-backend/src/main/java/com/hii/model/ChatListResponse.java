package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatListResponse {
  private List<ChatListItem> items;
}
