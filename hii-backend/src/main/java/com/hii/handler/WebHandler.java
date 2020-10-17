package com.hii.handler;

import com.hii.service.WebService;
import com.hii.util.HttpStatus;
import com.hii.util.JsonUtils;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebHandler extends BaseHandler {

  private static final Logger LOGGER = LogManager.getLogger(WebHandler.class);

  private WebService webService;

  public WebService getWebService() {
    return webService;
  }

  public void setWebService(WebService webService) {
    this.webService = webService;
  }

  public void signIn(RoutingContext routingContext) {
    String requestJson = routingContext.getBodyAsString();
    Future<JsonObject> signInFuture = webService.signIn(requestJson);

    signInFuture.compose(
        jsonObject -> {
          routingContext
              .response()
              .setStatusCode(HttpStatus.OK.code())
              .putHeader("content-type", "application/json; charset=utf-8")
              .end(JsonUtils.toSuccessJSON(jsonObject));
        },
        Future.future()
            .setHandler(
                handler -> {
                  handleException(handler.cause(), routingContext.response());
                }));
  }

  public void signOut(RoutingContext routingContext) {
    webService.signOut(routingContext);
  }
}
