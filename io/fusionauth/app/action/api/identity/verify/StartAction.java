package io.fusionauth.app.action.api.identity.verify;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.identity.verify.ExistingUserStrategy;
import io.fusionauth.domain.api.identity.verify.VerifyStartRequest;
import io.fusionauth.domain.api.identity.verify.VerifyStartResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class StartAction extends BaseTenantAPIAction {
  @JSONRequest
  public final VerifyStartRequest request = new VerifyStartRequest();
  
  private final UserService userService;
  
  @JSONResponse
  public VerifyStartResponse response;
  
  private UserService.IdentityValidationResult result;
  
  @Inject
  public StartAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.userService = paramUserService;
  }
  
  public String post() {
    if (this.request.existingUserStrategy == ExistingUserStrategy.mustExist && this.result.user == null)
      return "missing"; 
    ExternalIdentifier externalIdentifier = this.userService.startVerify(getTenant(), this.request.loginId, this.result.identityType, this.result.verificationStrategy, this.result.user, this.result.application, this.request.state);
    this.response = new VerifyStartResponse(externalIdentifier.getAttribute("otp"), externalIdentifier.id);
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.userService.validateStartVerify(getOptionalTenant(), this.request.applicationId, this.request.loginId, this.request.loginIdType, this.request.verificationStrategy, this.request.existingUserStrategy);
    conditionallyUpdateTenant(this.result.tenant);
    assertTenantResolved();
    this.frontEndSupport.transfer(this.result.errors);
  }
}
