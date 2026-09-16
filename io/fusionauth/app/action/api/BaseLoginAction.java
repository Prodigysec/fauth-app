package io.fusionauth.app.action.api;

import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.error.Error;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.authentication.AuthenticationService;
import io.fusionauth.api.service.authentication.LoginPreventedException;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.jwt.claims.JWTType;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.api.BaseLoginRequest;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.event.UserLoginNewDeviceEvent;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.http.Cookie;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.JSON.List;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.message.Message;
import org.primeframework.mvc.message.MessageType;
import org.primeframework.mvc.message.SimpleFieldMessage;
import org.primeframework.mvc.message.SimpleMessage;
import org.primeframework.mvc.scope.annotation.ManagedCookie;

@List({@Status(code = "expired", status = 410)})
@List({@JSON(code = "not-registered", status = 202), @JSON(code = "change-password", status = 203), @JSON(code = "identity-not-verified", status = 212), @JSON(code = "registration-not-verified", status = 213), @JSON(code = "link-required", status = 232), @JSON(code = "two-factor-challenge", status = 242), @JSON(code = "login-lambda-validation", status = 400), @JSON(code = "login-prevented", status = 409)})
public abstract class BaseLoginAction extends BaseTenantAPIAction {
  protected final ApplicationReaderService applicationReader;
  
  protected final JWTService jwtService;
  
  protected final RefreshTokenService refreshTokenService;
  
  @JSONResponse
  public LoginResponse response;
  
  @ManagedCookie(name = "fusionauth.trust", encrypt = false)
  public Cookie twoFactorTrustCookie;
  
  protected BaseLoginAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ApplicationReaderService paramApplicationReaderService, JWTService paramJWTService, RefreshTokenService paramRefreshTokenService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.applicationReader = paramApplicationReaderService;
    this.jwtService = paramJWTService;
    this.refreshTokenService = paramRefreshTokenService;
    this.useTenantCache = true;
  }
  
  protected <R extends BaseLoginRequest> void addTokensToResponse(Tenant paramTenant, Application paramApplication, R paramR, AuthenticationService.AuthenticationResult paramAuthenticationResult) {
    JWTService.JWTResult jWTResult;
    HashMap<Object, Object> hashMap = new HashMap<>(3);
    hashMap.put("aud", ((BaseLoginRequest)paramR).applicationId);
    UserRegistration userRegistration = paramAuthenticationResult.user.getRegistrationForApplication(((BaseLoginRequest)paramR).applicationId);
    if (userRegistration != null) {
      hashMap.put("applicationId", ((BaseLoginRequest)paramR).applicationId);
      hashMap.put("roles", userRegistration.roles);
    } 
    boolean bool1 = (paramAuthenticationResult.externalIdentifier != null && paramAuthenticationResult.externalIdentifier.type == ExternalIdentifier.ExternalIdType.OneTimePassword) ? true : false;
    boolean bool2 = (paramApplication != null && paramApplication.loginConfiguration.generateRefreshTokens) ? true : false;
    if (bool2 && bool1 && !paramAuthenticationResult.externalIdentifier.getAttributeAsBoolean("issueRefreshToken"))
      bool2 = false; 
    UUID uUID1 = bool2 ? UUID.randomUUID() : null;
    hashMap.put("sid", uUID1);
    UUID uUID2 = (paramApplication != null) ? paramApplication.lambdaConfiguration.accessTokenPopulateId : null;
    if (bool1) {
      if (!paramAuthenticationResult.externalIdentifier.getAttributeAsBoolean("issueJWT"))
        return; 
      ZonedDateTime zonedDateTime = paramAuthenticationResult.externalIdentifier.getAttributeAsZonedDateTime("jwtExpirationInstant");
      jWTResult = this.jwtService.createJWTWithExpiration(paramTenant, paramAuthenticationResult.user, paramAuthenticationResult.type, paramApplication, zonedDateTime, (Map)hashMap, uUID2, null, JWTType.AccessToken, Collections.emptySet());
    } else {
      jWTResult = this.jwtService.createJWT(paramTenant, paramAuthenticationResult.user, paramAuthenticationResult.type, paramApplication, (Map)hashMap, uUID2, null, JWTType.AccessToken, Collections.emptySet(), null);
    } 
    this.response.token = jWTResult.encodedJWT;
    this.response.tokenExpirationInstant = jWTResult.jwt.expiration;
    this.frontEndSupport.addHttpOnlySessionCookie("access_token", this.response.token);
    if (bool2) {
      Map<String, Object> map = Map.of("auth_time", Long.valueOf(jWTResult.jwt.issuedAt.toEpochSecond()), "source", "api");
      RefreshToken.MetaData metaData = (((BaseLoginRequest)paramR).eventInfo != null) ? ((BaseLoginRequest)paramR).eventInfo.toMetaData() : new RefreshToken.MetaData();
      ZonedDateTime zonedDateTime = bool1 ? paramAuthenticationResult.externalIdentifier.getAttributeAsZonedDateTime("refreshTokenStartInstant") : null;
      RefreshToken refreshToken = this.refreshTokenService.createRefreshTokenWithStartInstant(uUID1, paramTenant, paramAuthenticationResult.user, paramApplication, map, metaData, zonedDateTime);
      this.response.refreshToken = refreshToken.token;
      this.response.refreshTokenId = refreshToken.id;
      int i = (paramTenant.lookupJWTConfiguration(paramApplication)).refreshTokenTimeToLiveInMinutes;
      this.frontEndSupport.addHttpOnlyPersistentCookie("refresh_token", refreshToken.token, TimeUnit.MINUTES.toSeconds(i));
    } 
  }
  
  protected String callLogin(Tenant paramTenant, Application paramApplication, Supplier<AuthenticationService.AuthenticationResult> paramSupplier, BaseLoginRequest paramBaseLoginRequest) {
    AuthenticationService.AuthenticationResult authenticationResult;
    try {
      authenticationResult = paramSupplier.get();
      if (authenticationResult == null)
        return "missing"; 
      if (authenticationResult.exception != null)
        throw authenticationResult.exception; 
    } catch (LoginPreventedException loginPreventedException) {
      this.response = new LoginResponse(loginPreventedException.actions);
      return "login-prevented";
    } 
    if (authenticationResult.loginLambdaValidationResult != null && !authenticationResult.loginLambdaValidationResult.errors.empty()) {
      authenticationResult.loginLambdaValidationResult.errors.fieldErrors
        .forEach((paramString, paramList) -> paramList.forEach(()));
      authenticationResult.loginLambdaValidationResult.errors.generalErrors
        .forEach(paramError -> this.frontEndSupport.messageStore.add((Message)new SimpleMessage(MessageType.ERROR, paramError.code, paramError.message)));
      this.frontEndSupport.response.addHeader("X-FusionAuth-ValidationId", authenticationResult.loginValidationId.toString());
      return "login-lambda-validation";
    } 
    if (paramBaseLoginRequest.applicationId == null && authenticationResult.externalIdentifier != null)
      paramBaseLoginRequest.applicationId = authenticationResult.externalIdentifier.applicationId; 
    return handleAuthenticationResult(paramTenant, paramApplication, paramBaseLoginRequest, authenticationResult);
  }
  
  protected <R extends BaseLoginRequest> String handleAuthenticationResult(Tenant paramTenant, Application paramApplication, R paramR, AuthenticationService.AuthenticationResult paramAuthenticationResult) {
    if (paramAuthenticationResult.pendingIdPLinkId != null) {
      this
        .response = (new LoginResponse()).with(paramLoginResponse -> paramLoginResponse.pendingIdPLinkId = paramAuthenticationResult.pendingIdPLinkId).with(paramLoginResponse -> paramLoginResponse.threatsDetected = (paramAuthenticationResult.clientRisk != null) ? paramAuthenticationResult.clientRisk.getThreatsDetected() : Set.of());
      return "link-required";
    } 
    if (paramAuthenticationResult.user == null)
      return "missing"; 
    if (((BaseLoginRequest)paramR).newDevice) {
      UserLoginNewDeviceEvent userLoginNewDeviceEvent = new UserLoginNewDeviceEvent(((BaseLoginRequest)paramR).eventInfo, ((BaseLoginRequest)paramR).applicationId, paramAuthenticationResult.user.connectorId, paramAuthenticationResult.type.name(), paramAuthenticationResult.user);
      EventHelper.send(paramTenant, paramApplication, userLoginNewDeviceEvent);
    } 
    if (paramAuthenticationResult.twoFactorId != null) {
      this
        .response = (new LoginResponse()).with(paramLoginResponse -> paramLoginResponse.twoFactorId = paramAuthenticationResult.twoFactorId).with(paramLoginResponse -> paramLoginResponse.threatsDetected = (paramAuthenticationResult.clientRisk != null) ? paramAuthenticationResult.clientRisk.getThreatsDetected() : Set.of());
      this.response.methods = ((new User(paramAuthenticationResult.user)).secure()).twoFactor.methods;
      if (this.response.methods.isEmpty())
        this.response.configurableMethods = paramAuthenticationResult.configurableTwoFactorMethods; 
      this.twoFactorTrustCookie = null;
      return "two-factor-challenge";
    } 
    if (paramAuthenticationResult.user.passwordChangeRequired && paramAuthenticationResult.changePasswordId != null) {
      this



        
        .response = (new LoginResponse()).with(paramLoginResponse -> paramLoginResponse.changePasswordId = paramAuthenticationResult.changePasswordId).with(paramLoginResponse -> paramLoginResponse.changePasswordReason = paramAuthenticationResult.user.passwordChangeReason).with(paramLoginResponse -> paramLoginResponse.threatsDetected = (paramAuthenticationResult.clientRisk != null) ? paramAuthenticationResult.clientRisk.getThreatsDetected() : Set.of()).with(paramLoginResponse -> paramLoginResponse.twoFactorTrustId = paramAuthenticationResult.twoFactorTrustId);
      if (this.response.twoFactorTrustId != null)
        this.twoFactorTrustCookie.setValue(this.response.twoFactorTrustId); 
      return "change-password";
    } 
    boolean bool = (paramApplication != null && paramApplication.verificationStrategy == VerificationStrategy.FormField) ? true : false;
    this






      
      .response = (new LoginResponse(paramAuthenticationResult.user.secure().sort())).with(paramLoginResponse -> paramLoginResponse.emailVerificationId = paramAuthenticationResult.emailVerificationId).with(paramLoginResponse -> paramLoginResponse.identityVerificationId = paramAuthenticationResult.identityVerificationId).with(paramLoginResponse -> paramLoginResponse.registrationVerificationId = paramBoolean ? paramAuthenticationResult.registrationVerificationId : null).with(paramLoginResponse -> paramLoginResponse.threatsDetected = (paramAuthenticationResult.clientRisk != null) ? paramAuthenticationResult.clientRisk.getThreatsDetected() : Set.of()).with(paramLoginResponse -> paramLoginResponse.twoFactorTrustId = paramAuthenticationResult.twoFactorTrustId).with(paramLoginResponse -> paramLoginResponse.trustToken = paramAuthenticationResult.trustToken);
    if (this.response.twoFactorTrustId != null)
      this.twoFactorTrustCookie.setValue(this.response.twoFactorTrustId); 
    if (!((BaseLoginRequest)paramR).noJWT && !paramAuthenticationResult.trustOnly)
      addTokensToResponse(paramTenant, paramApplication, paramR, paramAuthenticationResult); 
    if (paramAuthenticationResult.externalIdentifier != null)
      this.response.state = paramAuthenticationResult.externalIdentifier.getStateHelper(); 
    if (paramAuthenticationResult.trustOnly)
      this.response.user = null; 
    if (paramAuthenticationResult.authenticatedNotRegistered)
      return "not-registered"; 
    UserRegistration userRegistration = paramAuthenticationResult.user.getRegistrationForApplication(((BaseLoginRequest)paramR).applicationId);
    if (userRegistration != null && !userRegistration.verified)
      return "registration-not-verified"; 
    UserIdentity userIdentity = paramAuthenticationResult.userIdentity;
    if (paramAuthenticationResult.userIdentity.type.is(IdentityType.username)) {
      UserIdentity userIdentity1 = paramAuthenticationResult.user.resolvePrimaryIdentity(IdentityType.email);
      if (userIdentity1 != null)
        userIdentity = userIdentity1; 
    } 
    if (userIdentity.verificationRequired())
      return "identity-not-verified"; 
    return "render";
  }
}
