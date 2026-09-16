package io.fusionauth.app.action.api.webauthn;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.webauthn.WebAuthnProviderService;
import io.fusionauth.app.action.api.BaseLoginAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.WebAuthnLoginRequest;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.http.Cookie;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class LoginAction extends BaseLoginAction {
  @JSONRequest
  public final WebAuthnLoginRequest request = new WebAuthnLoginRequest();
  
  private final AuthenticationService authenticationService;
  
  private final ExternalIdentifierReaderService externalIdentifierReaderService;
  
  private final ReactorStatusService reactorStatusService;
  
  private final WebAuthnProviderService webauthnProviderService;
  
  private WebAuthnProviderService.LoginValidationResult result;
  
  private ExternalIdentifierReaderService.ValidationResult twoFactorResult;
  
  @Inject
  protected LoginAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationReaderService paramApplicationReaderService, JWTService paramJWTService, RefreshTokenService paramRefreshTokenService, AuthenticationService paramAuthenticationService, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ReactorStatusService paramReactorStatusService, WebAuthnProviderService paramWebAuthnProviderService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramApplicationReaderService, paramJWTService, paramRefreshTokenService);
    this.authenticationService = paramAuthenticationService;
    this.externalIdentifierReaderService = paramExternalIdentifierReaderService;
    this.reactorStatusService = paramReactorStatusService;
    this.webauthnProviderService = paramWebAuthnProviderService;
  }
  
  public String post() {
    if (this.result.user == null || this.result.code == null)
      return "missing"; 
    return callLogin(getTenant(), this.result.application, () -> this.authenticationService.authenticateWebauthn(getTenant(), this.result.application, this.result.user, this.result.webauthnCredential, this.result.code, this.twoFactorResult.id, this.result.sigBase, this.result.requestCredential, this.request.eventInfo, this.request.botDetectionScore, this.request.newDevice), this.request);
  }
  
  @ValidationMethod
  public void validate() {
    Cookie cookie = this.frontEndSupport.getCookie("fusionauth.trust");
    if (cookie != null)
      this.request.twoFactorTrustId = cookie.value; 
    if (this.request.twoFactorTrustId != null) {
      this.twoFactorResult = this.externalIdentifierReaderService.validate(getOptionalTenant(), this.request.twoFactorTrustId, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TwoFactorTrust });
    } else {
      this.twoFactorResult = new ExternalIdentifierReaderService.ValidationResult();
    } 
    this.result = this.webauthnProviderService.validateLoginComplete(getOptionalTenant(), this.request.credential);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors, this.result.partialEventLog);
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
