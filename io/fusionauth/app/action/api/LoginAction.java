package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.util.ClaimTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.LoginPingRequest;
import io.fusionauth.domain.api.LoginRequest;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.http.Cookie;
import io.fusionauth.jwt.domain.JWT;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.security.annotation.AuthorizeMethod;
import org.primeframework.mvc.security.annotation.JWTAuthorizeMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userId}/{applicationId}", requiresAuthentication = true, scheme = {"api", "authorize-method", "scoped-jwt"})
public class LoginAction extends BaseLoginAction {
  @JSONRequest(httpMethods = {"PUT"})
  public final LoginPingRequest putRequest = new LoginPingRequest();
  
  @JSONRequest(httpMethods = {"POST"})
  public final LoginRequest request = new LoginRequest();
  
  private final AuthenticationService authenticationService;
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  public UUID applicationId;
  
  public String ipAddress;
  
  public UUID userId;
  
  private ExternalIdentifierReaderService.ValidationResult oneTimePasswordResult;
  
  private AuthenticationService.ValidationResult result;
  
  private ExternalIdentifierReaderService.ValidationResult twoFactorResult;
  
  @Inject
  public LoginAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationReaderService paramApplicationReaderService, AuthenticationService paramAuthenticationService, JWTService paramJWTService, ExternalIdentifierReaderService paramExternalIdentifierReaderService, RefreshTokenService paramRefreshTokenService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache, paramApplicationReaderService, paramJWTService, paramRefreshTokenService);
    this.authenticationService = paramAuthenticationService;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
  }
  
  @AuthorizeMethod(httpMethods = {"POST"})
  public boolean authorize() {
    if (this.request.eventInfo == null)
      this.request.eventInfo = new EventInfo(); 
    this.request.eventInfo.ipAddress = this.frontEndSupport.getTrustedClientIPAddress();
    if (this.request.applicationId != null) {
      Application application = this.applicationReader.retrieveById(null, this.request.applicationId);
      if (application != null)
        return !application.loginConfiguration.requireAuthentication; 
      return true;
    } 
    return !(getTenant()).loginConfiguration.requireAuthentication;
  }
  
  @JWTAuthorizeMethod(httpMethods = {"PUT"})
  public boolean authorizeJWT(JWT paramJWT) {
    if (this.request.eventInfo == null)
      this.request.eventInfo = new EventInfo(); 
    this.request.eventInfo.ipAddress = this.frontEndSupport.getTrustedClientIPAddress();
    try {
      if (paramJWT.subject != null) {
        this.putRequest.userId = UUID.fromString(paramJWT.subject);
        this.putRequest.applicationId = ClaimTools.resolveApplicationId(paramJWT);
        return true;
      } 
    } catch (Exception exception) {}
    return false;
  }
  
  @PostParameterMethod
  public void normalize() {
    this.request.normalize();
  }
  
  public String post() {
    if (this.request.oneTimePassword != null) {
      if (this.oneTimePasswordResult.id == null)
        return "missing"; 
      return callLogin(getTenant(), this.result.application, () -> this.authenticationService.authenticateOneTimePassword(getTenant(), this.result.application, this.oneTimePasswordResult.id, this.twoFactorResult.id, this.request.eventInfo, this.request.botDetectionScore, this.request.newDevice), this.request);
    } 
    return callLogin(getTenant(), this.result.application, () -> this.authenticationService.authenticate(getTenant(), this.result.application, this.request.loginId, this.result.identityTypes, this.request.password, this.twoFactorResult.id, this.request.eventInfo, this.request.botDetectionScore, this.request.newDevice), this.request);
  }
  
  public String put() {
    if (this.result.user == null)
      return "missing"; 
    AuthenticationService.AuthenticationResult authenticationResult = this.authenticationService.ping(getTenant(), this.result.user, this.result.application, this.putRequest.eventInfo);
    this.response = new LoginResponse();
    this.response.emailVerificationId = authenticationResult.emailVerificationId;
    this.response.registrationVerificationId = authenticationResult.registrationVerificationId;
    if (authenticationResult.authenticatedNotRegistered)
      return "not-registered"; 
    if (authenticationResult.userIdentity != null && authenticationResult.userIdentity.verificationRequired())
      return "identity-not-verified"; 
    if (this.result.application != null && !(this.result.user.getRegistrationForApplication(this.result.application.id)).verified) {
      UserRegistration userRegistration = this.result.user.getRegistrationForApplication(this.result.application.id);
      if (userRegistration != null && !userRegistration.verified)
        return "registration-not-verified"; 
    } 
    return "render";
  }
  
  public void setApplicationId(UUID paramUUID) {
    this.putRequest.applicationId = paramUUID;
  }
  
  public void setIpAddress(String paramString) {
    if (this.putRequest.eventInfo == null)
      this.putRequest.eventInfo = new EventInfo(); 
    this.putRequest.eventInfo.ipAddress = paramString;
  }
  
  public void setUserId(UUID paramUUID) {
    this.putRequest.userId = paramUUID;
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    Cookie cookie = this.frontEndSupport.getCookie("fusionauth.trust");
    if (cookie != null)
      this.request.twoFactorTrustId = cookie.value; 
    if (this.request.twoFactorTrustId != null) {
      this.twoFactorResult = this.externalIdentifierReader.validate(getOptionalTenant(), this.request.twoFactorTrustId, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TwoFactorTrust });
      conditionallyUpdateTenant(this.twoFactorResult.tenant);
    } else {
      this.twoFactorResult = new ExternalIdentifierReaderService.ValidationResult();
    } 
    if (this.request.oneTimePassword != null) {
      this.oneTimePasswordResult = this.externalIdentifierReader.validate(getOptionalTenant(), this.request.oneTimePassword, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.OneTimePassword });
      conditionallyUpdateTenant(this.oneTimePasswordResult.tenant);
      this.result = this.authenticationService.validateOneTimePassword(getOptionalTenant(), this.request.applicationId, this.oneTimePasswordResult.id);
    } else {
      this.result = this.authenticationService.validateAuthenticate(getOptionalTenant(), this.request.applicationId, this.request.loginId, this.request.password, this.request.loginIdTypes, this.request.botDetectionScore);
    } 
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT"})
  public void validatePut() {
    this.result = this.authenticationService.validatePing(getOptionalTenant(), this.putRequest.userId, this.putRequest.applicationId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
