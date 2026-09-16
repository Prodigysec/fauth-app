package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.AuditLogSearchCriteria;
import java.time.ZoneId;

public class AuditLogExportRequest extends BaseExportRequest {
  public AuditLogSearchCriteria criteria;
  
  @JacksonConstructor
  public AuditLogExportRequest() {}
  
  public AuditLogExportRequest(AuditLogSearchCriteria paramAuditLogSearchCriteria, String paramString, ZoneId paramZoneId) {
    this.criteria = paramAuditLogSearchCriteria;
    this.dateTimeSecondsFormat = paramString;
    this.zoneId = paramZoneId;
  }
}
