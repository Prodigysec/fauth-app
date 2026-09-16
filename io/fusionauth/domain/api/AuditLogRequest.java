package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.EventInfo;

public class AuditLogRequest extends BaseEventRequest {
  public AuditLog auditLog;
  
  @JacksonConstructor
  public AuditLogRequest() {}
  
  public AuditLogRequest(AuditLog paramAuditLog) {
    this.auditLog = paramAuditLog;
  }
  
  public AuditLogRequest(EventInfo paramEventInfo, AuditLog paramAuditLog) {
    super(paramEventInfo);
    this.auditLog = paramAuditLog;
  }
}
