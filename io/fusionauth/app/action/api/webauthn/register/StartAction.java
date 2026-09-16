package io.fusionauth.app.action.api.webauthn.register;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.webauthn.WebAuthnProviderService;
import io.fusionauth.app.action.api.webauthn.BaseWebAuthnAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.WebAuthnRegisterStartRequest;
import io.fusionauth.domain.api.WebAuthnRegisterStartResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api"})
public class StartAction extends BaseWebAuthnAction {
  @JSONRequest
  public final WebAuthnRegisterStartRequest request = new WebAuthnRegisterStartRequest();
  
  private final WebAuthnProviderService webauthnProviderService;
  
  @JSONResponse
  public WebAuthnRegisterStartResponse response;
  
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
    this.response = new WebAuthnRegisterStartResponse(this.webauthnProviderService.buildCreationOptionsResponse(getTenant(), this.result.user, this.request.workflow, this.request.displayName, this.request.name, this.request.userAgent));
    return "render";
  }
  
  @ValidationMethod
  public void validate() {
    this.result = this.webauthnProviderService.validateRegistrationStart(getOptionalTenant(), this.request.userId, this.request.workflow, this.request.displayName);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
