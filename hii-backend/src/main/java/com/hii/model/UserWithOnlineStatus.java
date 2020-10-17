package com.hii.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserWithOnlineStatus extends UserLite {
  boolean isOnline;
}
