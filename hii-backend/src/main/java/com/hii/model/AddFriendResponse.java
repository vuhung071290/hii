package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AddFriendResponse implements Serializable {
  private AddressBookItem item;
}
