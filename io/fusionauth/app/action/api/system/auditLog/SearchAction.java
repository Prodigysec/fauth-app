package io.fusionauth.app.action.api.system.auditLog;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.system.AuditService;
import io.fusionauth.app.action.api.BaseSearchAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.AuditLogSearchRequest;
import io.fusionauth.domain.api.AuditLogSearchResponse;
import io.fusionauth.domain.search.AuditLogSearchCriteria;
import io.fusionauth.domain.search.BaseSearchCriteria;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;

@Action(requiresAuthentication = true, scheme = {"api"})
public class SearchAction extends BaseSearchAPIAction<AuditLogSearchCriteria> {
  @JSONRequest
  public final AuditLogSearchRequest request = new AuditLogSearchRequest();
  
  private final AuditService auditService;
  
  @JSONResponse
  public AuditLogSearchResponse response;
  
  @Inject
  public SearchAction(AuthenticationKeyCache paramAuthenticationKeyCache, FrontEndSupport paramFrontEndSupport, AuditService paramAuditService) {
    super(paramAuthenticationKeyCache, paramFrontEndSupport);
    this.auditService = paramAuditService;
  }
  
  public ZonedDateTime getEnd() {
    return (criteria()).end;
  }
  
  public void setEnd(ZonedDateTime paramZonedDateTime) {
    (criteria()).end = paramZonedDateTime;
  }
  
  public String getMessage() {
    return (criteria()).message;
  }
  
  public void setMessage(String paramString) {
    (criteria()).message = paramString;
  }
  
  public String getNewValue() {
    return (criteria()).newValue;
  }
  
  public void setNewValue(String paramString) {
    (criteria()).newValue = paramString;
  }
  
  public String getOldValue() {
    return (criteria()).oldValue;
  }
  
  public void setOldValue(String paramString) {
    (criteria()).oldValue = paramString;
  }
  
  public String getReason() {
    return (criteria()).reason;
  }
  
  public void setReason(String paramString) {
    (criteria()).reason = paramString;
  }
  
  public ZonedDateTime getStart() {
    return (criteria()).start;
  }
  
  public void setStart(ZonedDateTime paramZonedDateTime) {
    (criteria()).start = paramZonedDateTime;
  }
  
  public UUID getTenantId() {
    return (criteria()).tenantId;
  }
  
  public void setTenantId(UUID paramUUID) {
    (criteria()).tenantId = paramUUID;
  }
  
  public String getUser() {
    return (criteria()).user;
  }
  
  public void setUser(String paramString) {
    (criteria()).user = paramString;
  }
  
  protected AuditLogSearchCriteria criteria() {
    return this.request.search;
  }
  
  protected String search() {
    if (tenantIdWasSpecified())
      (criteria()).tenantId = this.tenant.id; 
    this.response = new AuditLogSearchResponse(this.auditService.search(this.request.search));
    return "render";
  }
}
