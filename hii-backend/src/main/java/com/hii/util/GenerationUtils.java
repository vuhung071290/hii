package com.hii.util;

import java.util.UUID;

public final class GenerationUtils {
  private GenerationUtils() {}

  public static String generateId() {
    return UUID.randomUUID().toString();
  }
}
