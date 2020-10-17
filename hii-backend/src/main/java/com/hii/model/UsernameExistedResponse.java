package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UsernameExistedResponse implements Serializable {
  private boolean existed;
  private String username;
}
