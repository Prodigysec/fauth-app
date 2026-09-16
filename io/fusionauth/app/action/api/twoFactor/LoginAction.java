package io.fusionauth.app.action.api.twoFactor;

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
import io.fusionauth.domain.api.twoFactor.TwoFactorLoginRequest;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class LoginAction extends BaseLoginAction {
  @JSONRequest
  public final TwoFactorLoginRequest request = new TwoFactorLoginRequest();
  
  private final AuthenticationService authenticationService;
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private ExternalIdentifierReaderService.ValidationResult idResult;
  
  private AuthenticationService.ValidationResult result;
  
  @Inject
  public LoginAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationReaderService paramApplicationReaderService, JWTService paramJWTService, AuthenticationService paramAuthenticationService, ExternalIdentifierReaderService paramExternalIdentifierReaderService, RefreshTokenService paramRefreshTokenService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramApplicationReaderService, paramJWTService, paramRefreshTokenService);
    this.authenticationService = paramAuthenticationService;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
  }
  
  public String post() {
    if (this.idResult.id == null)
      return "missing"; 
    return callLogin(getTenant(), this.result.application, () -> this.authenticationService.authenticateTwoFactor(getTenant(), this.result.application, this.request.code, this.idResult.id, this.request.trustComputer, this.request.eventInfo), this.request);
  }
  
  @ValidationMethod
  public void validate() {
    this.idResult = this.externalIdentifierReader.validate(getOptionalTenant(), this.request.twoFactorId, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TwoFactor });
    conditionallyUpdateTenant(this.idResult.tenant);
    if (this.idResult.id != null && this.request.applicationId == null)
      this.request.applicationId = this.idResult.id.applicationId; 
    this.result = this.authenticationService.validateTwoFactor(getOptionalTenant(), this.request.applicationId, this.request.code, this.request.twoFactorId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
