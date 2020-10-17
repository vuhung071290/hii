package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserStatus implements Serializable {
  private String userId;
  private String status;
}
