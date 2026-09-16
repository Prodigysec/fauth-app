package io.fusionauth.app.action.api.webauthn;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.webauthn.WebAuthnProviderService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.WebAuthnAssertResponse;
import io.fusionauth.domain.api.WebAuthnLoginRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class AssertAction extends BaseWebAuthnAction {
  @JSONRequest
  public final WebAuthnLoginRequest request = new WebAuthnLoginRequest();
  
  private final WebAuthnProviderService webauthnProviderService;
  
  @JSONResponse
  public WebAuthnAssertResponse response;
  
  private WebAuthnProviderService.LoginValidationResult result;
  
  @Inject
  protected AssertAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ReactorStatusService paramReactorStatusService, WebAuthnProviderService paramWebAuthnProviderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramReactorStatusService);
    this.webauthnProviderService = paramWebAuthnProviderService;
  }
  
  public String post() {
    if (this.result.user == null || this.result.code == null)
      return "missing"; 
    this.response = new WebAuthnAssertResponse(this.webauthnProviderService.completeAssertion(getTenant(), this.result.user, this.result.webauthnCredential, this.result.requestCredential, this.result.sigBase));
    if (this.response.credential == null)
      return "missing"; 
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.webauthnProviderService.validateLoginComplete(getOptionalTenant(), this.request.credential);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors, this.result.partialEventLog);
  }
}
