package com.hii.verticle;

import com.hii.api.ApiServer;
import com.hii.api.WebsocketServer;
import com.hii.cache.client.RedisCacheClient;
import com.hii.handler.ProtectedApiHandler;
import com.hii.handler.PublicApiHandler;
import com.hii.handler.WebHandler;
import com.hii.handler.WsHandler;
import com.hii.manager.JwtManager;
import com.hii.manager.UserWsChannelManager;
import com.hii.repository.DataRepository;
import com.hii.service.APIService;
import com.hii.service.WebService;
import com.hii.util.PropertiesUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class HiiVerticle extends AbstractVerticle {

  private ApiServer apiServer;
  private WebsocketServer websocketServer;

  @Override
  public void start(Future<Void> future) {
    log.info("{} verticle {} start", deploymentID(), Thread.currentThread().getName());

    this.apiServer = ApiServer.newInstance();
    this.websocketServer = WebsocketServer.newInstance();

    // Create a JWT Auth Provider
    log.info("Initial JWT for verticle {}", Thread.currentThread().getName());
    JwtManager jwtManager = new JwtManager(vertx);

    // Inject dependency
    log.info("Starting Inject Dependency for verticle {}", Thread.currentThread().getName());
    RedisClient client =
        RedisClient.create(
            vertx,
            new RedisOptions().setHost(PropertiesUtils.getInstance().getValue("redis.host")));
    DataRepository repository = new RedisCacheClient(client);

    UserWsChannelManager userWsChannelManager = new UserWsChannelManager();
    userWsChannelManager.setEventBus(vertx.eventBus());
    userWsChannelManager.setSharedData(vertx.sharedData());

    APIService apiService = new APIService();
    apiService.setDataRepository(repository);
    apiService.setUserWsChannelManager(userWsChannelManager);

    WebService webService = new WebService();
    webService.setDataRepository(repository);
    webService.setJwtManager(jwtManager);

    ProtectedApiHandler protectedApiHandler = new ProtectedApiHandler();
    protectedApiHandler.setDataRepository(repository);
    protectedApiHandler.setJwtManager(jwtManager);
    protectedApiHandler.setApiService(apiService);

    PublicApiHandler publicApiHandler = new PublicApiHandler();
    publicApiHandler.setDataRepository(repository);
    publicApiHandler.setApiService(apiService);

    WebHandler webHandler = new WebHandler();
    webHandler.setDataRepository(repository);
    webHandler.setWebService(webService);

    WsHandler wsHandler = new WsHandler();
    wsHandler.setDataRepository(repository);
    wsHandler.setApiService(apiService);

    apiServer.setProtectedApiHandler(protectedApiHandler);
    apiServer.setPublicApiHandler(publicApiHandler);
    apiServer.setWebHandler(webHandler);

    wsHandler.setUserWsChannelManager(userWsChannelManager);

    websocketServer.setWsHandler(wsHandler);
    websocketServer.setUserWsChannelManager(userWsChannelManager);
    websocketServer.setJwtManager(jwtManager);

    log.info("Inject Dependency successfully for verticle {}", Thread.currentThread().getName());

    Future.succeededFuture()
        .compose(v -> apiServer.createHttpServer(vertx))
        .compose(v -> websocketServer.createWsServer(vertx))
        .setHandler(future);
  }

  @Override
  public void stop() {
    log.info("Shutting down application");
  }
}
