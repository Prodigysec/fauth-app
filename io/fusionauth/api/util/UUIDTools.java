package io.fusionauth.api.util;

import java.util.UUID;

public class UUIDTools {
  public static UUID fromString(String paramString) {
    try {
      return UUID.fromString(paramString);
    } catch (Exception exception) {
      return null;
    } 
  }
}
