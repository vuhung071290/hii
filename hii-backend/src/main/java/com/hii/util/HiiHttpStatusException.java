package com.hii.util;

import io.netty.handler.codec.http.HttpResponseStatus;

public class HiiHttpStatusException extends RuntimeException {
  private final int statusCode;
  private final String code;
  private final String payload;

  public HiiHttpStatusException(int statusCode, String code, String payload) {
    super(HttpResponseStatus.valueOf(statusCode).reasonPhrase(), null, false, false);
    this.statusCode = statusCode;
    this.code = code;
    this.payload = payload;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getCode() {
    return code;
  }

  public String getPayload() {
    return payload;
  }
}
