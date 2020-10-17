package com.hii.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class WsMessage implements IWsMessage {
  protected String type;
}
