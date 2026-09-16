package io.fusionauth.api.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimeWrapper {
  protected static volatile ZonedDateTime override;
  
  private static boolean testMode;
  
  public static ZonedDateTime now(ZoneId paramZoneId) {
    if (testMode && override != null)
      return override.withZoneSameInstant(paramZoneId); 
    return ZonedDateTime.now(paramZoneId);
  }
  
  protected static void enableTestMode() {
    testMode = true;
  }
}
