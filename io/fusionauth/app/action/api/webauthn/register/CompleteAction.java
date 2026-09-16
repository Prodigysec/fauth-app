package io.fusionauth.app.action.api.webauthn.register;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.webauthn.WebAuthnProviderService;
import io.fusionauth.app.action.api.webauthn.BaseWebAuthnAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.WebAuthnRegisterCompleteRequest;
import io.fusionauth.domain.api.WebAuthnRegisterCompleteResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class CompleteAction extends BaseWebAuthnAction {
  @JSONRequest
  public final WebAuthnRegisterCompleteRequest request = new WebAuthnRegisterCompleteRequest();
  
  private final WebAuthnProviderService webauthnProviderService;
  
  @JSONResponse
  public WebAuthnRegisterCompleteResponse response;
  
  private WebAuthnProviderService.CompleteValidationResult result;
  
  @Inject
  protected CompleteAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ReactorStatusService paramReactorStatusService, WebAuthnProviderService paramWebAuthnProviderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramReactorStatusService);
    this.webauthnProviderService = paramWebAuthnProviderService;
  }
  
  public String post() {
    assertTenantResolved();
    if (this.result.user == null || this.result.code == null)
      return "missing"; 
    this.response = new WebAuthnRegisterCompleteResponse(this.webauthnProviderService.completeRegistration(getTenant(), this.result.user, this.result.credential, this.result.publicKey, this.result.code));
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.webauthnProviderService.validateRegistrationComplete(getOptionalTenant(), this.request.userId, this.request.credential);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors, this.result.partialEventLog);
  }
}
