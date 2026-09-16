package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.AuditLogSearchCriteria;

public class AuditLogSearchRequest {
  public AuditLogSearchCriteria search = new AuditLogSearchCriteria();
  
  @JacksonConstructor
  public AuditLogSearchRequest() {}
  
  public AuditLogSearchRequest(AuditLogSearchCriteria paramAuditLogSearchCriteria) {
    this.search = paramAuditLogSearchCriteria;
  }
}
