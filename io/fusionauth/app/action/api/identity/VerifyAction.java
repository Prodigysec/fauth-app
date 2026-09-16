package io.fusionauth.app.action.api.identity;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.identity.verify.VerifyRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class VerifyAction extends BaseTenantAPIAction {
  @JSONRequest
  public final VerifyRequest request = new VerifyRequest();
  
  private final UserService userService;
  
  private UserService.IdentityValidationResult result;
  
  @Inject
  public VerifyAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.userService = paramUserService;
  }
  
  public String post() {
    if (this.result.user == null)
      return "missing"; 
    this.userService.forceVerifyIdentity(getTenant(), this.result.user, this.result.identity, this.request.eventInfo);
    return "success";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.userService.validateAdministrativeVerify(getTenant(), this.request.loginId, this.request.loginIdType);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
