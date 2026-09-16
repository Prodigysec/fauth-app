package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import io.fusionauth.api.service.useraction.UserActionReasonService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.UserActionReason;
import io.fusionauth.domain.api.UserActionReasonRequest;
import io.fusionauth.domain.api.UserActionReasonResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userActionReasonId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class UserActionReasonAction extends BaseAPIAction implements Patchable {
  private final UserActionReasonService userActionReasonService;
  
  @JSONPatch
  @JSONRequest
  public UserActionReasonRequest request = new UserActionReasonRequest();
  
  @JSONResponse
  public UserActionReasonResponse response;
  
  @PreParameter
  public UUID userActionReasonId;
  
  private UserActionReasonService.ValidationResult result;
  
  @Inject
  public UserActionReasonAction(FrontEndSupport paramFrontEndSupport, UserActionReasonService paramUserActionReasonService) {
    super(paramFrontEndSupport);
    this.userActionReasonService = paramUserActionReasonService;
  }
  
  public String delete() {
    if (!this.userActionReasonService.delete(this.userActionReasonId))
      return "missing"; 
    return "success";
  }
  
  public String get() {
    if (this.userActionReasonId == null) {
      this.response = new UserActionReasonResponse(this.userActionReasonService.retrieveAll());
      return "render";
    } 
    UserActionReason userActionReason = this.userActionReasonService.retrieveById(this.userActionReasonId);
    if (userActionReason == null)
      return "missing"; 
    this.response = new UserActionReasonResponse(userActionReason);
    return "render";
  }
  
  public void loadExisting() {
    if (this.userActionReasonId != null)
      this.request.userActionReason = this.userActionReasonService.retrieveById(this.userActionReasonId); 
  }
  
  public String post() {
    this.userActionReasonService.create(this.request.userActionReason);
    this.response = new UserActionReasonResponse(this.request.userActionReason);
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    this.userActionReasonService.update(this.result.existing, this.request.userActionReason);
    this.response = new UserActionReasonResponse(this.request.userActionReason);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT", "PATCH"})
  public void validate() {
    if (this.request == null || this.request.userActionReason == null) {
      this.frontEndSupport.addFieldError("userActionReason", "[missing]userActionReason", new Object[0]);
      return;
    } 
    this.request.userActionReason.id = this.userActionReasonId;
    this.request.userActionReason.normalize();
    this.result = this.userActionReasonService.validate(this.request.userActionReason, this.frontEndSupport.isPOST());
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.userActionReasonId == null)
      this.frontEndSupport.addFieldError("userActionId", "[missing]userActionId", new Object[0]); 
  }
}
