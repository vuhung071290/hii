package com.hii.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class GetSessionIdResponse implements Serializable {
  private String sessionId;
}
