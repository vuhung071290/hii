package com.hii.verticle;

import com.hii.cache.client.RedisCacheClient;
import com.hii.repository.DataRepository;
import com.hii.service.InitDataService;
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
public class InitDataVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> future) {
    log.info("{} verticle {} start", deploymentID(), Thread.currentThread().getName());

    // Inject dependency
    log.info("Starting Inject Dependency for verticle {}", Thread.currentThread().getName());
    RedisClient client =
        RedisClient.create(
            vertx,
            new RedisOptions().setHost(PropertiesUtils.getInstance().getValue("redis.host")));
    DataRepository dataRepository = new RedisCacheClient(client);

    InitDataService initDataService = new InitDataService();
    initDataService.setDataRepository(dataRepository);

    initDataService
        .initData()
        .compose(
            jsonObject -> {
              log.info(jsonObject.toString());
            },
            Future.future()
                .setHandler(
                    handler -> {
                      log.error(handler.cause().getMessage());
                    }));
  }

  @Override
  public void stop() {
    log.info("Shutting down application");
  }
}
