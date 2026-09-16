package io.fusionauth.app.action.api.passwordless;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.app.action.api.BaseLoginAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.passwordless.PasswordlessLoginRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class LoginAction extends BaseLoginAction {
  @JSONRequest
  public final PasswordlessLoginRequest request = new PasswordlessLoginRequest();
  
  private final AuthenticationService authenticationService;
  
  private final ExternalIdentifierReaderService externalIdentifierReaderService;
  
  private ExternalIdentifierReaderService.ValidationResult codeResult;
  
  private ExternalIdentifierReaderService.ValidationResult twoFactorResult;
  
  @Inject
  public LoginAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationReaderService paramApplicationReaderService, JWTService paramJWTService, AuthenticationService paramAuthenticationService, ExternalIdentifierReaderService paramExternalIdentifierReaderService, RefreshTokenService paramRefreshTokenService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramApplicationReaderService, paramJWTService, paramRefreshTokenService);
    this.authenticationService = paramAuthenticationService;
    this.externalIdentifierReaderService = paramExternalIdentifierReaderService;
  }
  
  public String post() {
    if (this.codeResult.id == null)
      return "missing"; 
    return callLogin(getTenant(), this.codeResult.application, () -> this.authenticationService.authenticatePasswordless(getTenant(), this.codeResult.application, this.codeResult.id, this.twoFactorResult.id, this.request.eventInfo, this.request.botDetectionScore, this.request.newDevice), this.request);
  }
  
  @ValidationMethod
  public void validate() {
    if (this.twoFactorTrustCookie.getValue() != null)
      this.request.twoFactorTrustId = this.twoFactorTrustCookie.getValue(); 
    this.codeResult = this.externalIdentifierReaderService.validate(getOptionalTenant(), this.request.code, ExternalIdentifier.ExternalIdType.PasswordlessLogin, this.request.twoFactorTrustId, ExternalIdentifier.ExternalIdType.TwoFactorTrust);
    conditionallyUpdateTenant(this.codeResult.tenant);
    if (this.codeResult.application != null && !this.codeResult.application.passwordlessConfiguration.enabled) {
      this.frontEndSupport.addGeneralError("[PasswordlessDisabled]", new Object[0]);
      return;
    } 
    this.frontEndSupport.transfer(this.authenticationService.validatePasswordless(this.request.code, this.request.oneTimeCode, this.codeResult.id));
    if (this.request.twoFactorTrustId != null) {
      this.twoFactorResult = this.externalIdentifierReaderService.validate(getOptionalTenant(), this.request.twoFactorTrustId, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TwoFactorTrust });
    } else {
      this.twoFactorResult = new ExternalIdentifierReaderService.ValidationResult();
    } 
  }
}
