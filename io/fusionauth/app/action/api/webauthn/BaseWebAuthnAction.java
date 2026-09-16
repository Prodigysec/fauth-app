package io.fusionauth.app.action.api.webauthn;

import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import org.primeframework.mvc.validation.ValidationMethod;

public abstract class BaseWebAuthnAction extends BaseTenantAPIAction {
  private final ReactorStatusService reactorStatusService;
  
  protected BaseWebAuthnAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ReactorStatusService paramReactorStatusService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.reactorStatusService = paramReactorStatusService;
  }
  
  @ValidationMethod
  public void validateLicense() {
    ReactorStatus reactorStatus = this.reactorStatusService.retrieveStatus();
    if (!reactorStatus.licensed) {
      this.frontEndSupport.addGeneralError("[notLicensed]", new Object[0]);
    } else if (ReactorStatusValidator.isNotLicensedFor(reactorStatus, paramReactorStatus -> paramReactorStatus.webAuthn)) {
      this.frontEndSupport.addGeneralError("[notLicensedFor]", new Object[0]);
    } 
  }
}
