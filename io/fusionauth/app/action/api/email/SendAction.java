package io.fusionauth.app.action.api.email;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.user.DefaultUserService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.email.SendRequest;
import io.fusionauth.domain.api.email.SendResponse;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{emailTemplateId}", requiresAuthentication = true, scheme = {"api"})
public class SendAction extends BaseTenantAPIAction {
  private final UserService userService;
  
  public UUID emailTemplateId;
  
  @JSONRequest
  public SendRequest request;
  
  @JSONResponse
  public SendResponse response = new SendResponse();
  
  private UserService.ValidationResult result;
  
  @Inject
  public SendAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.userService = paramUserService;
  }
  
  public String post() {
    if (this.result.emailTemplate == null)
      return "missing"; 
    if (this.result.users.isEmpty() && this.request.toAddresses.isEmpty())
      return "success"; 
    DefaultUserService.EmailSendResult emailSendResult = this.userService.sendEmail(getTenant(), this.result.application, this.result.emailTemplate, this.result.users, this.request.toAddresses, this.request.ccAddresses, this.request.bccAddresses, this.request.requestData, this.request.preferredLanguages);
    this.response.results = emailSendResult.userIdErrors;
    this.response.anonymousResults = emailSendResult.emailErrors;
    return "accepted";
  }
  
  @ValidationMethod
  public void validate() {
    this.request.normalize();
    this.result = this.userService.validateEmailSend(getOptionalTenant(), this.request.applicationId, this.request.userIds, this.request.toAddresses, this.emailTemplateId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
