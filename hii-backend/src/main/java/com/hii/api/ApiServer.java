package com.hii.api;

import com.hii.handler.ProtectedApiHandler;
import com.hii.handler.PublicApiHandler;
import com.hii.handler.WebHandler;
import com.hii.util.PropertiesUtils;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
@Setter
public final class ApiServer {

  private HttpServer httpServer;
  private ProtectedApiHandler protectedApiHandler;
  private PublicApiHandler publicApiHandler;
  private WebHandler webHandler;

  private ApiServer() {}

  public static ApiServer newInstance() {
    ApiServer apiServer = new ApiServer();
    return apiServer;
  }

  public Future<Void> createHttpServer(Vertx vertx) {
    if (httpServer != null) Future.succeededFuture();
    log.info("Starting API Server ...");

    Router router = Router.router(vertx);

    router
        .route("/")
        .handler(
            routingContext -> {
              HttpServerResponse httpServerResponse = routingContext.response();
              httpServerResponse
                  .putHeader("content-type", "text/html")
                  .end("<h1>Web app back-end</h1>");
            });

    router.route().handler(BodyHandler.create());

    Set<String> allowedHeaders = new HashSet<>();
    allowedHeaders.add("x-requested-with");
    allowedHeaders.add("Access-Control-Allow-Origin");
    allowedHeaders.add("origin");
    allowedHeaders.add("accept");
    allowedHeaders.add("Content-Type");
    allowedHeaders.add("Authorization");

    Set<HttpMethod> allowedMethods = new HashSet<>();
    allowedMethods.add(HttpMethod.GET);
    allowedMethods.add(HttpMethod.POST);
    allowedMethods.add(HttpMethod.OPTIONS);
    allowedMethods.add(HttpMethod.DELETE);
    allowedMethods.add(HttpMethod.PATCH);
    allowedMethods.add(HttpMethod.PUT);

    router
        .route("/*")
        .handler(
            CorsHandler.create(
                    "http://"
                        + PropertiesUtils.getInstance().getValue("nodejs.host")
                        + ":"
                        + PropertiesUtils.getInstance().getValue("nodejs.port"))
                .allowedHeaders(allowedHeaders)
                .allowedMethods(allowedMethods)
                .allowCredentials(true))
        .handler(BodyHandler.create());

    router.post("/signin").handler(webHandler::signIn);
    router.post("/signout").handler(webHandler::signOut);

    router.route("/api/protected/*").handler(protectedApiHandler::handle);

    router.post("/api/public/*").handler(publicApiHandler::handle);

    Future future = Future.future();
    httpServer =
        vertx
            .createHttpServer()
            .requestHandler(router::accept)
            .exceptionHandler(
                exHandler -> {
                  log.error(exHandler.getCause().getMessage());
                })
            .listen(
                PropertiesUtils.getInstance().getIntValue("api.port"),
                ar -> {
                  if (ar.succeeded()) {
                    log.info("API Server start successfully !");
                    future.complete();
                  } else {
                    log.error("API Server start fail. Reason: {}", ar.cause().getMessage());
                    future.fail(ar.cause());
                  }
                });

    return future;
  }
}
