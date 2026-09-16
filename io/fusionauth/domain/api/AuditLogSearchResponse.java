package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class AuditLogSearchResponse {
  public List<AuditLog> auditLogs;
  
  public long total;
  
  @JacksonConstructor
  public AuditLogSearchResponse() {}
  
  public AuditLogSearchResponse(SearchResults<AuditLog> paramSearchResults) {
    this.auditLogs = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
