package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.AuditLog;

public class AuditLogResponse {
  public AuditLog auditLog;
  
  @JacksonConstructor
  public AuditLogResponse() {}
  
  public AuditLogResponse(AuditLog paramAuditLog) {
    this.auditLog = paramAuditLog;
  }
}
