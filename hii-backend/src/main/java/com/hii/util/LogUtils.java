package com.hii.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LogUtils {

  private static final Logger USER_LOGGER = LogManager.getLogger("user-tracking-Logger");

  private LogUtils() {}

  public static void userLog(String message) {
    USER_LOGGER.info(message);
  }
}
