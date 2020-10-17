package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class FriendList implements Serializable {
  private UserHash currentUserHashes;
  private UserHash friendUserHashes;
}
