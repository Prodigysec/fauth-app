package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import io.fusionauth.api.service.useraction.UserActionService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.api.UserActionRequest;
import io.fusionauth.domain.api.UserActionResponse;
import java.util.Comparator;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userActionId}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class UserActionAction extends BaseAPIAction implements Patchable {
  private final UserActionService userActionService;
  
  public boolean hardDelete;
  
  public boolean inactive;
  
  public boolean reactivate;
  
  @JSONPatch
  @JSONRequest
  public UserActionRequest request = new UserActionRequest();
  
  @JSONResponse
  public UserActionResponse response;
  
  @PreParameter
  public UUID userActionId;
  
  @Inject
  public UserActionAction(FrontEndSupport paramFrontEndSupport, UserActionService paramUserActionService) {
    super(paramFrontEndSupport);
    this.userActionService = paramUserActionService;
  }
  
  public String delete() {
    if (this.hardDelete) {
      if (!this.userActionService.delete(this.userActionId))
        return "missing"; 
    } else if (!this.userActionService.deactivate(this.userActionId)) {
      return "missing";
    } 
    return "success";
  }
  
  public String get() {
    if (this.userActionId == null) {
      if (this.inactive) {
        this.response = new UserActionResponse(this.userActionService.retrieveAllInactive());
        this.response.userActions.sort(Comparator.comparing(paramUserAction -> paramUserAction.name));
      } else {
        this.response = new UserActionResponse(this.userActionService.retrieveAll());
        this.response.userActions.sort(Comparator.comparing(paramUserAction -> paramUserAction.name));
      } 
      return "render";
    } 
    UserAction userAction = this.userActionService.retrieveByIdIgnoreActive(this.userActionId);
    if (userAction == null)
      return "missing"; 
    this.response = new UserActionResponse(userAction);
    return "render";
  }
  
  public void loadExisting() {
    if (this.userActionId != null)
      this.request.userAction = this.userActionService.retrieveById(this.userActionId); 
  }
  
  public String post() {
    this.userActionService.create(this.request.userAction);
    this.response = new UserActionResponse(this.request.userAction);
    return "render";
  }
  
  public String put() {
    if (this.reactivate) {
      this.request.userAction = this.userActionService.reactivate(this.userActionId);
      if (this.request.userAction == null)
        return "missing"; 
    } else if (!this.userActionService.update(this.request.userAction)) {
      return "missing";
    } 
    this.response = new UserActionResponse(this.request.userAction);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"POST", "PUT", "PATCH"})
  public void validate() {
    if (this.reactivate) {
      if (this.userActionId == null)
        this.frontEndSupport.addFieldError("userActionId", "[missing]userActionId", new Object[0]); 
      return;
    } 
    if (this.request == null || this.request.userAction == null) {
      this.frontEndSupport.addFieldError("userAction", "[missing]userAction", new Object[0]);
      return;
    } 
    this.request.userAction.id = this.userActionId;
    this.request.userAction.normalize();
    Errors errors = this.userActionService.validate(this.request.userAction, this.frontEndSupport.isPOST());
    this.frontEndSupport.transfer(errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    if (this.userActionId == null)
      this.frontEndSupport.addFieldError("userActionId", "[missing]userActionId", new Object[0]); 
  }
}
