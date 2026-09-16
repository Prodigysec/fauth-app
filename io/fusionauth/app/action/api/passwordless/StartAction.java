package io.fusionauth.app.action.api.passwordless;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.user.PasswordlessService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.passwordless.PasswordlessStartRequest;
import io.fusionauth.domain.api.passwordless.PasswordlessStartResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class StartAction extends BaseTenantAPIAction {
  @JSONRequest
  public final PasswordlessStartRequest request = new PasswordlessStartRequest();
  
  private final PasswordlessService passwordlessService;
  
  @JSONResponse
  public PasswordlessStartResponse response;
  
  private PasswordlessService.ValidationResult result;
  
  @Inject
  public StartAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, PasswordlessService paramPasswordlessService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.passwordlessService = paramPasswordlessService;
  }
  
  public String post() {
    if (this.result.user == null)
      return "missing"; 
    PasswordlessService.PasswordlessCode passwordlessCode = this.passwordlessService.createCode(getTenant(), this.result.application, this.result.user, this.request.loginId, this.result.identityTypes, this.request.state, this.result.strategy);
    this.response = new PasswordlessStartResponse(passwordlessCode.code(), passwordlessCode.otp());
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.passwordlessService.validateStart(getOptionalTenant(), this.request.applicationId, this.request.loginId, this.request.loginIdTypes, this.request.loginStrategy);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
