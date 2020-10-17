package com.hii.handler;

import com.hii.repository.DataRepository;
import com.hii.util.HiiHttpStatusException;
import com.hii.util.HttpStatus;
import com.hii.util.JsonUtils;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public abstract class BaseHandler {

  protected DataRepository dataRepository;

  public void handleException(Throwable throwable, HttpServerResponse response) {

    if (throwable instanceof HiiHttpStatusException) {
      HiiHttpStatusException e = (HiiHttpStatusException) throwable;
      JsonObject obj = new JsonObject();
      obj.put("code", e.getCode());
      obj.put("message", e.getPayload());
      response
          .setStatusCode(e.getStatusCode())
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(JsonUtils.toErrorJSON(obj));
      return;
    }

    if (throwable instanceof Exception) {
      log.error(throwable.getMessage());
      throwable.printStackTrace();
      response
          .setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.code())
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(JsonUtils.toError500JSON());
      return;
    }
  }

  public void ping(HttpServerRequest request, HttpServerResponse response) {
    response
        .setStatusCode(HttpStatus.OK.code())
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(JsonUtils.toSuccessJSON("Pong"));
  }
}
