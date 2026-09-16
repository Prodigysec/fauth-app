package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.webauthn.WebAuthnProviderService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.WebAuthnCredentialResponse;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{id}", requiresAuthentication = true, scheme = {"api"})
public class WebauthnAction extends BaseTenantAPIAction {
  private final ReactorStatusService reactorStatusService;
  
  private final WebAuthnProviderService webauthnProviderService;
  
  public UUID id;
  
  @JSONResponse
  public WebAuthnCredentialResponse response;
  
  public UUID userId;
  
  private WebAuthnProviderService.ValidationResult result;
  
  @Inject
  public WebauthnAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ReactorStatusService paramReactorStatusService, WebAuthnProviderService paramWebAuthnProviderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.reactorStatusService = paramReactorStatusService;
    this.webauthnProviderService = paramWebAuthnProviderService;
  }
  
  public String delete() {
    if (this.result.user != null) {
      this.webauthnProviderService.deleteByUserId(this.result.user.id);
      return "success";
    } 
    if (this.result.existing == null)
      return "missing"; 
    this.webauthnProviderService.delete(this.result.existing);
    return "success";
  }
  
  public String get() {
    if (this.result.user != null) {
      this.response = new WebAuthnCredentialResponse(this.webauthnProviderService.retrieveAllByUserId(getTenant(), this.result.user.id));
      return "render";
    } 
    if (this.result.existing == null)
      return "missing"; 
    this.response = new WebAuthnCredentialResponse(this.result.existing);
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET", "DELETE"})
  public void validateGetDelete() {
    this.result = this.webauthnProviderService.validateCredentialGetDelete(getOptionalTenant(), this.id, this.userId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateLicense() {
    ReactorStatus reactorStatus = this.reactorStatusService.retrieveStatus();
    if (!reactorStatus.licensed) {
      this.frontEndSupport.addGeneralError("[notLicensed]", new Object[0]);
    } else if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.webAuthn)) {
      this.frontEndSupport.addGeneralError("[notLicensedFor]", new Object[0]);
    } 
  }
}
