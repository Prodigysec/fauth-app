package io.fusionauth.app.action.api.passwordless;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.user.PasswordlessService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.passwordless.PasswordlessSendRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class SendAction extends BaseTenantAPIAction {
  @JSONRequest
  public final PasswordlessSendRequest request = new PasswordlessSendRequest();
  
  private final PasswordlessService passwordlessService;
  
  private PasswordlessService.ValidationResult result;
  
  @Inject
  public SendAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, PasswordlessService paramPasswordlessService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.passwordlessService = paramPasswordlessService;
  }
  
  public String post() {
    if ((this.request.code != null && this.result.code == null) || this.result.user == null)
      return "success"; 
    this.passwordlessService.sendCode(getTenant(), this.result.application, this.result.user, this.request.loginId, this.result.code, this.request.state);
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.passwordlessService.validateSend(getOptionalTenant(), this.request.applicationId, this.request.loginId, this.request.code);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
