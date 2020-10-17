package com.hii.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAuth {
  private String userName;
  private String userId;
  private String hashedPassword;
}
