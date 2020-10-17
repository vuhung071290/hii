package com.hii.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressBookItem {
  private String name;
  private String userId;
  private String status;
  private boolean online;
}
