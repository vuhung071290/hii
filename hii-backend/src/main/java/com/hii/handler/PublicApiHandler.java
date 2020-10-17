package com.hii.handler;

import com.hii.model.User;
import com.hii.service.APIService;
import com.hii.util.HttpStatus;
import com.hii.util.JsonUtils;
import com.hii.util.LogUtils;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class PublicApiHandler extends BaseHandler {

  private APIService apiService;

  public void handle(RoutingContext rc) {
    HttpServerRequest request = rc.request();
    HttpServerResponse response = rc.response();
    String requestPath = request.path();
    String path = StringUtils.substringAfter(requestPath, "/api/public");
    String json = rc.getBodyAsString();

    switch (path) {
      case "/user":
        LogUtils.userLog("New register request");
        registerUser(request, response, json);
        break;
      default:
        response.end();
        break;
    }
  }

  public void registerUser(
      HttpServerRequest request, HttpServerResponse response, String jsonData) {

    Future<User> registerUserFuture = apiService.registerUser(jsonData);

    registerUserFuture.compose(
        user -> {
          response
              .setStatusCode(HttpStatus.OK.code())
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(JsonUtils.toSuccessJSON(user));
        },
        Future.future()
            .setHandler(
                handler -> {
                  handleException(handler.cause(), response);
                }));
  }

}
