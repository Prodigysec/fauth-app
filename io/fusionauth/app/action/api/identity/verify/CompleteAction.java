package io.fusionauth.app.action.api.identity.verify;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.identity.verify.VerifyCompleteRequest;
import io.fusionauth.domain.api.identity.verify.VerifyCompleteResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class CompleteAction extends BaseTenantAPIAction {
  @JSONRequest
  public final VerifyCompleteRequest request = new VerifyCompleteRequest();
  
  @JSONResponse
  public final VerifyCompleteResponse response = new VerifyCompleteResponse();
  
  private final UserService userService;
  
  private UserService.IdentityValidationResult result;
  
  @Inject
  public CompleteAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.userService = paramUserService;
  }
  
  public String post() {
    if (this.result.externalId == null)
      return "missing"; 
    this.userService.completeVerify(getTenant(), this.result.application, this.result.user, this.result.externalId, this.request.eventInfo);
    this.response.state = this.result.externalId.getStateHelper();
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.userService.validateCompleteVerify(getOptionalTenant(), this.request.verificationId, this.request.oneTimeCode);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
