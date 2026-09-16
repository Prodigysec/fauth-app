package io.fusionauth.app.action.api.twoFactor;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.mfa.MFAService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.twoFactor.TwoFactorStartRequest;
import io.fusionauth.domain.api.twoFactor.TwoFactorStartResponse;
import java.util.List;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class StartAction extends BaseTenantAPIAction {
  @JSONRequest
  public final TwoFactorStartRequest request = new TwoFactorStartRequest();
  
  private final MFAService mfaService;
  
  @JSONResponse
  public TwoFactorStartResponse response;
  
  private MFAService.ValidationResult result;
  
  @Inject
  public StartAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, MFAService paramMFAService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.mfaService = paramMFAService;
  }
  
  public String post() {
    assertTenantResolved();
    if (this.result.user == null)
      return "missing"; 
    MFAService.StartTwoFactorResult startTwoFactorResult = this.mfaService.startTwoFactorRequest(getTenant(), this.result.application, this.result.user, this.result.userIdentity, this.request.code, this.request.trustChallenge, this.request.state, this.frontEndSupport
        .buildEventInfo());
    List<TwoFactorMethod> list = ((new User(this.result.user)).secure()).twoFactor.methods;
    this.response = new TwoFactorStartResponse(startTwoFactorResult.code, list, startTwoFactorResult.twoFactorId);
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.mfaService.validateStart(getOptionalTenant(), this.request.applicationId, this.request.userId, this.request.loginId, this.request.loginIdTypes);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
