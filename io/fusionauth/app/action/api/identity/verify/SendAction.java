package io.fusionauth.app.action.api.identity.verify;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.identity.verify.VerifySendRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class SendAction extends BaseTenantAPIAction {
  @JSONRequest
  public final VerifySendRequest request = new VerifySendRequest();
  
  private final UserService userService;
  
  private UserService.IdentityValidationResult result;
  
  @Inject
  public SendAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.userService = paramUserService;
  }
  
  public String post() {
    if (this.result.externalId == null)
      return "missing"; 
    this.userService.sendVerify(getTenant(), this.result.externalId, this.result.application, this.result.user);
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.userService.validateSendVerify(getOptionalTenant(), this.request.verificationId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
