package com.hii;

import com.hii.model.User;
import com.hii.model.UserAuth;
import com.hii.model.UserFull;
import com.hii.repository.DataRepository;
import com.hii.service.APIService;
import com.hii.service.WebService;
import com.hii.util.PropertiesUtils;
import com.hii.verticle.HiiVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Slf4j
@Getter
@Setter
public class BaseVerticleTestSuite {

  public static final String RESOURCE_PATH = "src/main/resources/";
  private static final String PROP_FILE_NAME = "system.properties";

  private static HiiVerticle hiiVerticle;

  private static APIService apiService;

  private static WebService webService;

  private static String userIdToTest;

  private static String anotherUserIdToTest;

  private static String yetAnotherUserIdToTest;

  private static UserFull userFullToTest;

  private static UserFull anotherUserFullToTest;

  private static String sessionIdToTest;

  private static String jwtToTest;

  private static String jwtAuthHeader;

  private static String host = "localhost";

  private static int port = 8080;

  private static DataRepository dataRepository;

  private static HttpClient client;

  private static String env;

  public static APIService getApiService() {
    return apiService;
  }

  public static WebService getWebService() {
    return webService;
  }

  public static DataRepository getDataRepository() {
    return dataRepository;
  }

  public static String getUserIdToTest() {
    return userIdToTest;
  }

  public static String getAnotherUserIdToTest() {
    return anotherUserIdToTest;
  }

  public static String getYetAnotherUserIdToTest() {
    return yetAnotherUserIdToTest;
  }

  public static UserFull getUserFullToTest() {
    return userFullToTest;
  }

  public static UserFull getAnotherUserFullToTest() {
    return anotherUserFullToTest;
  }

  public static HttpClient getClient() {
    return client;
  }

  public static String getJwtToTest() {
    return jwtToTest;
  }

  public static String getHost() {
    return host;
  }

  public static int getPort() {
    return port;
  }

  public static String getJwtAuthHeader() {
    return jwtAuthHeader;
  }

  public static String getSessionIdToTest() {
    return PropertiesUtils.getInstance().getValue("test.session.id");
  }

  @BeforeClass
  public static void setUp(TestContext context) {
    if (hiiVerticle == null) {
      env = System.getenv("env");
      if (StringUtils.isBlank(env)) {
        log.error("Missing env");
        System.exit(1);
      }
      initSystemProperty(context);
      initVertx(context);
    }
  }

  private static void initSystemProperty(TestContext context) {
    Properties p = new Properties();
    try {
      p.load(FileUtils.openInputStream(new File(RESOURCE_PATH + env + "." + PROP_FILE_NAME)));
    } catch (IOException e) {
      log.error("Cannot load System Property");
      System.exit(1);
    }
    for (String name : p.stringPropertyNames()) {
      String value = p.getProperty(name);
      System.setProperty(name, value);
    }
  }

  private static void initVertx(TestContext context) {
    final Async async = context.async();
    Vertx vertx = Vertx.vertx();
    hiiVerticle = new HiiVerticle();
    vertx.deployVerticle(
        hiiVerticle,
        ar -> {
          apiService = hiiVerticle.getApiServer().getProtectedApiHandler().getApiService();
          webService = hiiVerticle.getApiServer().getWebHandler().getWebService();
          dataRepository = hiiVerticle.getApiServer().getProtectedApiHandler().getDataRepository();
          client = vertx.createHttpClient();
          Future<UserAuth> getUserAuthFuture =
              getApiService().getDataRepository().getUserAuth("usera");
          Future<UserAuth> getUserAuthFuture2 =
              getApiService().getDataRepository().getUserAuth("userb");
          Future<UserAuth> getUserAuthFuture3 =
              getApiService().getDataRepository().getUserAuth("userc");
          CompositeFuture compositeFuture =
              CompositeFuture.all(getUserAuthFuture, getUserAuthFuture2, getUserAuthFuture3);
          compositeFuture.setHandler(
              res -> {
                userIdToTest = ((UserAuth) compositeFuture.resultAt(0)).getUserId();
                anotherUserIdToTest = ((UserAuth) compositeFuture.resultAt(1)).getUserId();
                yetAnotherUserIdToTest = ((UserAuth) compositeFuture.resultAt(2)).getUserId();
                User user = new User();
                user.setUserName("usera");
                user.setPassword("123");
                getWebService()
                    .signIn(Json.encodePrettily(user))
                    .setHandler(
                        res2 -> {
                          jwtToTest = res2.result().getString("jwt");
                          jwtAuthHeader = "Bearer " + jwtToTest;
                          Future<UserFull> getUserFullFuture =
                              getApiService().getDataRepository().getUserFull(userIdToTest);
                          Future<UserFull> getUserFullFuture2 =
                              getApiService().getDataRepository().getUserFull(anotherUserIdToTest);
                          CompositeFuture compositeFuture2 =
                              CompositeFuture.all(getUserFullFuture, getUserFullFuture2);
                          compositeFuture2.setHandler(
                              res3 -> {
                                userFullToTest = ((UserFull) compositeFuture2.resultAt(0));
                                anotherUserFullToTest = ((UserFull) compositeFuture2.resultAt(1));
                                async.complete();
                              });
                        });
              });
        });
  }
}
