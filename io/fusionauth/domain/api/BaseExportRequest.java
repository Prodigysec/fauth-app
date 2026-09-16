package io.fusionauth.domain.api;

import java.time.ZoneId;

public abstract class BaseExportRequest {
  public String dateTimeSecondsFormat;
  
  public ZoneId zoneId;
}
