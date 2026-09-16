package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.useraction.ActionService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.api.user.ActionRequest;
import io.fusionauth.domain.api.user.ActionResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{actionId}", requiresAuthentication = true, scheme = {"api"})
public class ActionAction extends BaseTenantAPIAction {
  @JSONRequest
  public final ActionRequest request = new ActionRequest();
  
  private final ActionService actionService;
  
  public UUID actionId;
  
  public Boolean active;
  
  public boolean preventingLogin;
  
  @JSONResponse
  public ActionResponse response;
  
  public UUID userId;
  
  private ActionService.ValidationResult result;
  
  @Inject
  public ActionAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ActionService paramActionService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.actionService = paramActionService;
  }
  
  public String delete() {
    if (this.result.log == null)
      return "missing"; 
    UserActionLog userActionLog = this.actionService.cancelAction(this.result.actionerTenant, this.result.actioneeUser, this.result.action, this.result.log, this.request.action, this.request.broadcast, this.request.eventInfo);
    this.response = new ActionResponse(userActionLog);
    return "render";
  }
  
  public String get() {
    if (this.actionId != null) {
      if (this.result.log == null)
        return "missing"; 
      this.response = new ActionResponse(this.result.log);
      return "render";
    } 
    if (this.preventingLogin) {
      this.response = new ActionResponse(this.actionService.retrieveAllForUserPreventingLogin(this.result.actioneeUser));
    } else {
      this.response = new ActionResponse(this.actionService.retrieveAllForUser(this.result.actioneeUser, this.active));
    } 
    return "render";
  }
  
  public String post() {
    UserActionLog userActionLog = this.actionService.actionUser(this.result.actionerTenant, this.result.actioneeTenant, this.result.actioneeUser, this.result.action, this.result.reason, this.request.action, this.request.broadcast, this.request.eventInfo);
    this.response = new ActionResponse(userActionLog);
    return "render";
  }
  
  public String put() {
    if (this.result.log == null)
      return "missing"; 
    UserActionLog userActionLog = this.actionService.updateAction(this.result.actionerTenant, this.result.actioneeUser, this.result.action, this.result.log, this.request.action, this.request.broadcast, this.request.eventInfo);
    this.response = new ActionResponse(userActionLog);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE", "POST", "PUT"})
  public void validate() {
    if (this.request.action == null) {
      this.frontEndSupport.addFieldError("action", "[missing]action", new Object[0]);
      return;
    } 
    this.request.action.normalize();
    if (this.frontEndSupport.isPOST()) {
      this.result = this.actionService.validate(getOptionalTenantId(), this.request.action);
    } else {
      this.result = this.actionService.validateUpdateOrEnd(getOptionalTenantId(), this.actionId, this.request.action, this.frontEndSupport.isPUT());
    } 
    conditionallyUpdateTenant(this.result.actioneeTenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.result = this.actionService.validateRetrieve(this.actionId, this.userId);
    conditionallyUpdateTenant(this.result.actioneeTenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
