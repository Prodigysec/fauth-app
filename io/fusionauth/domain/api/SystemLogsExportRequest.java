package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import java.time.ZoneId;

public class SystemLogsExportRequest extends BaseExportRequest {
  public boolean includeArchived;
  
  public int lastNBytes = 65536;
  
  @JacksonConstructor
  public SystemLogsExportRequest() {}
  
  public SystemLogsExportRequest(String paramString, ZoneId paramZoneId, int paramInt, boolean paramBoolean) {
    this.dateTimeSecondsFormat = paramString;
    this.zoneId = paramZoneId;
    this.lastNBytes = paramInt;
    this.includeArchived = paramBoolean;
  }
}
