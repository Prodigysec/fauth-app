package io.fusionauth.app.action.api.system;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.system.AuditService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.AuditLogRequest;
import io.fusionauth.domain.api.AuditLogResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{id}", requiresAuthentication = true, scheme = {"api"})
public class AuditLogAction extends BaseTenantAPIAction {
  @JSONRequest
  public final AuditLogRequest request = new AuditLogRequest();
  
  private final AuditService auditService;
  
  public Integer id;
  
  @JSONResponse
  public AuditLogResponse response;
  
  @Inject
  public AuditLogAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, AuditService paramAuditService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.auditService = paramAuditService;
  }
  
  public String get() {
    this.response = new AuditLogResponse(this.auditService.retrieveById(tenantIdWasSpecified() ? getOptionalTenantId() : null, this.id.intValue()));
    return "render";
  }
  
  public String post() {
    if (tenantIdWasSpecified())
      this.request.auditLog.tenantId = getOptionalTenantId(); 
    this.auditService.create(this.request.auditLog, this.request.eventInfo);
    this.response = new AuditLogResponse(this.request.auditLog);
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    if (this.request.auditLog == null) {
      this.frontEndSupport.addFieldError("auditLog", "[missing]auditLog", new Object[0]);
      return;
    } 
    this.request.auditLog.normalize();
    Errors errors = this.auditService.validate(this.request.auditLog);
    this.frontEndSupport.transfer(errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.id == null)
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]); 
  }
}
