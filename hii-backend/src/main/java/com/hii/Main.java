package com.hii;

import com.hii.util.PropertiesUtils;
import com.hii.verticle.HiiVerticle;
import com.hii.verticle.InitDataVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public class Main {
  public static final String RESOURCE_PATH = "src/main/resources/";
  private static final String PROP_FILE_NAME = "system.properties";
  private static String env;

  public static void main(String[] args) throws IOException {
    env = System.getenv("env");
    if (StringUtils.isBlank(env)) {
      log.error("Missing env");
      System.exit(1);
    }
    System.setProperty("env", env);
    initSystemProperty();
    initVertx();
  }

  private static void initSystemProperty() {
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

  private static void initVertx() {
    VertxOptions vertxOptions = new VertxOptions();
    vertxOptions.setWorkerPoolSize(PropertiesUtils.getInstance().getIntValue("worker.size"));
    vertxOptions.setMaxEventLoopExecuteTime(Long.MAX_VALUE);

    DeploymentOptions deploymentOptions = new DeploymentOptions();

    if (PropertiesUtils.getInstance().getIntValue("init.data") == 1) {

      vertxOptions.setMaxEventLoopExecuteTime(10000000000L);

      deploymentOptions.setInstances(1);

      Vertx vertx = Vertx.vertx(vertxOptions);
      vertx.deployVerticle(InitDataVerticle.class, deploymentOptions);
    } else {

      deploymentOptions.setInstances(PropertiesUtils.getInstance().getIntValue("eventLoopPoolSize"));

      Vertx vertx = Vertx.vertx(vertxOptions);
      vertx.deployVerticle(HiiVerticle.class, deploymentOptions);
    }
  }
}
