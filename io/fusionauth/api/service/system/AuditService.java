package io.fusionauth.api.service.system;

import com.inversoft.error.Errors;
import io.fusionauth.domain.AuditLog;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.search.AuditLogSearchCriteria;
import io.fusionauth.domain.search.SearchResults;
import java.io.OutputStream;
import java.time.ZoneId;
import java.util.UUID;
import javax.annotation.Nullable;

public interface AuditService {
  void create(AuditLog paramAuditLog, EventInfo paramEventInfo);
  
  void exportSearchResults(OutputStream paramOutputStream, AuditLogSearchCriteria paramAuditLogSearchCriteria, String paramString, ZoneId paramZoneId);
  
  AuditLog retrieveById(@Nullable UUID paramUUID, int paramInt);
  
  SearchResults<AuditLog> search(AuditLogSearchCriteria paramAuditLogSearchCriteria);
  
  Errors validate(AuditLog paramAuditLog);
}
