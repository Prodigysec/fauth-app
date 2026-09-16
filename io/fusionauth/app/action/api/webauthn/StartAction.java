package io.fusionauth.app.action.api.webauthn;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.webauthn.WebAuthnProviderService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.WebAuthnStartRequest;
import io.fusionauth.domain.api.WebAuthnStartResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class StartAction extends BaseWebAuthnAction {
  @JSONRequest
  public final WebAuthnStartRequest request = new WebAuthnStartRequest();
  
  private final WebAuthnProviderService webauthnProviderService;
  
  @JSONResponse
  public WebAuthnStartResponse response;
  
  private WebAuthnProviderService.StartValidationResult result;
  
  @Inject
  protected StartAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ReactorStatusService paramReactorStatusService, WebAuthnProviderService paramWebAuthnProviderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramReactorStatusService);
    this.webauthnProviderService = paramWebAuthnProviderService;
  }
  
  public String post() {
    assertTenantResolved();
    if (this.result.user == null)
      return "missing"; 
    this.response = new WebAuthnStartResponse(this.webauthnProviderService.buildRequestOptionsResponse(getTenant(), this.result.application, this.result.user, this.result.userIdentity, this.result.credentials, this.request.state, this.request.workflow));
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.webauthnProviderService.validateLoginStart(getOptionalTenant(), this.request.applicationId, this.request.userId, this.request.loginId, this.request.loginIdTypes, this.request.credentialId, this.request.workflow);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
