package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.util.CollectionTools;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.jwt.claims.JWTType;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.api.service.user.VerificationIdResponseHelper;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.action.api.Patchable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.user.RegistrationDeleteRequest;
import io.fusionauth.domain.api.user.RegistrationRequest;
import io.fusionauth.domain.api.user.RegistrationResponse;
import io.fusionauth.domain.jwt.RefreshToken;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userId}/{applicationId}", requiresAuthentication = true, scheme = {"api"})
public class RegistrationAction extends BaseTenantAPIAction implements Patchable {
  @JSONRequest(httpMethods = {"DELETE"})
  public final RegistrationDeleteRequest deleteRequest = new RegistrationDeleteRequest();
  
  private final JWTService jwtService;
  
  private final RefreshTokenService refreshTokenService;
  
  private final UserReaderService userReader;
  
  private final UserService userService;
  
  @PreParameter
  public UUID applicationId;
  
  @JSONPatch
  @JSONRequest(httpMethods = {"PATCH", "POST", "PUT"})
  public RegistrationRequest request = new RegistrationRequest();
  
  @JSONResponse
  public RegistrationResponse response;
  
  @PreParameter
  public UUID userId;
  
  private UserService.ValidationResult result;
  
  private UserService.ValidationResult resultForUser;
  
  @Inject
  public RegistrationAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, JWTService paramJWTService, RefreshTokenService paramRefreshTokenService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.jwtService = paramJWTService;
    this.refreshTokenService = paramRefreshTokenService;
    this.userReader = paramUserReaderService;
    this.userService = paramUserService;
  }
  
  public String delete() {
    if (this.result.registration == null)
      return "missing"; 
    this.userService.deleteRegistration(getTenant(), this.result.registration, this.result.user, this.result.application, this.deleteRequest.eventInfo);
    return "success";
  }
  
  public String get() {
    if (this.result.registration == null)
      return "missing"; 
    this.response = new RegistrationResponse(null, this.result.registration);
    this.response.registrationVerificationId = this.result.registrationVerificationId;
    this.response.registrationVerificationOneTimeCode = this.result.registrationVerificationOneTimeCode;
    return "render";
  }
  
  public void loadExisting() {
    if (this.userId != null && this.applicationId != null)
      this.request.registration = this.userReader.retrieveRegistration(getOptionalTenantId(), this.userId, this.applicationId); 
  }
  
  public String post() {
    User user = Optional.<UserService.ValidationResult>ofNullable(this.resultForUser).map(paramValidationResult -> paramValidationResult.user).orElse(null);
    UserService.UserResult userResult = null;
    if (user != null) {
      boolean bool = !this.result.application.verifyRegistration ? true : false;
      userResult = this.userService.create(getTenant(), this.result.application, user, this.resultForUser.sendSetPasswordIdentityType, this.request.skipVerification, false, bool, true, this.request.eventInfo, this.resultForUser.preVerifiedExternalIds);
      this.result.user = user;
    } 
    UserService.RegistrationResult registrationResult = this.userService.createRegistration(getTenant(), this.result.application, this.result.user, this.request.registration, this.result.roles, this.request.generateAuthenticationToken, this.request.skipRegistrationVerification, true, this.request.eventInfo);
    this.response = new RegistrationResponse((user != null) ? user.secure() : null, this.request.registration);
    if (userResult != null)
      this.response.verificationIds = VerificationIdResponseHelper.getSortedVerificationIds(userResult.verificationIds, null); 
    this.response.registrationVerificationId = registrationResult.registrationVerificationId;
    this.response.registrationVerificationOneTimeCode = registrationResult.registrationVerificationOneTimeCode;
    addTokenToResponse((new User(this.result.user)).with(paramUser -> paramUser.getRegistrations().add(this.request.registration)));
    return "render";
  }
  
  public String put() {
    if (this.result.registration == null)
      return "missing"; 
    UserService.RegistrationResult registrationResult = this.userService.updateRegistration(getTenant(), this.result.application, this.result.user, this.result.registration, this.request.registration, this.result.roles, this.request.generateAuthenticationToken, this.request.eventInfo);
    this.response = new RegistrationResponse(null, registrationResult.registration);
    this.response.registrationVerificationId = registrationResult.registrationVerificationId;
    this.response.registrationVerificationOneTimeCode = registrationResult.registrationVerificationOneTimeCode;
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    this.result = this.userService.validateRegistrationDelete(getOptionalTenant(), this.userId, this.applicationId);
    if (tenantScopedKeyInvalidForUser(this.result.application, this.result.user))
      this.result.registration = null; 
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    this.result = this.userService.validateRegistrationRetrieve(getOptionalTenant(), this.userId, this.applicationId);
    if (tenantScopedKeyInvalidForUser(this.result.application, this.result.user))
      this.result.registration = null; 
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.registration == null) {
      this.frontEndSupport.addFieldError("registration", "[missing]registration", new Object[0]);
      return;
    } 
    this.request.registration.normalize();
    boolean bool1 = !this.request.verificationIds.isEmpty() ? true : false;
    boolean bool2 = (this.request.user != null || bool1) ? true : false;
    this.result = this.userService.validateRegistrationCreate(getOptionalTenant(), this.request.registration, this.userId, bool2, this.request.generateAuthenticationToken);
    this.frontEndSupport.transfer(this.result.errors);
    if (tenantScopedKeyInvalidForUser(this.result.application, this.result.user) && !bool2)
      this.frontEndSupport.addFieldError("userId", "[invalid]userId", new Object[] { this.userId }); 
    if (!bool2) {
      conditionallyUpdateTenant(this.result.tenant);
      return;
    } 
    normalizeRequestUser();
    if (bool1) {
      this.resultForUser = this.userService.validatePreVerifiedCreate(this.result.tenant, this.request.user, this.request.registration.applicationId, this.request.verificationIds, this.request.disableDomainBlock, this.request.sendSetPasswordIdentityType, this.request.sendSetPasswordEmail, this.request.eventInfo);
      conditionallyUpdateTenant(Optional.<Tenant>ofNullable(this.result.tenant).orElse(this.resultForUser.tenant));
      this.frontEndSupport.transfer(this.resultForUser.errors);
    } else if (this.result.tenant != null) {
      conditionallyUpdateTenant(this.result.tenant);
      this.resultForUser = this.userService.validateCreate(getTenant(), this.request.user, this.request.registration.applicationId, this.request.disableDomainBlock, this.request.sendSetPasswordIdentityType, this.request.sendSetPasswordEmail, this.request.eventInfo, false);
      this.frontEndSupport.transfer(this.resultForUser.errors);
    } 
  }
  
  @ValidationMethod(httpMethods = {"PATCH", "PUT"})
  public void validatePutAndPatch() {
    if (this.request.registration == null) {
      this.frontEndSupport.addFieldError("registration", "[missing]registration", new Object[0]);
      return;
    } 
    this.request.registration.normalize();
    this.request.registration.applicationId = (this.applicationId != null) ? this.applicationId : this.request.registration.applicationId;
    this.result = this.userService.validateRegistrationUpdate(getOptionalTenant(), this.request.registration, this.userId, this.request.generateAuthenticationToken);
    if (tenantScopedKeyInvalidForUser(this.result.application, this.result.user)) {
      this.result.registration = null;
      return;
    } 
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  private void addTokenToResponse(User paramUser) {
    UUID uUID = this.result.application.loginConfiguration.generateRefreshTokens ? UUID.randomUUID() : null;
    Map<String, UUID> map = CollectionTools.mapNV(new Object[] { "applicationId", this.result.application.id, "amr", 
          List.of("none"), "aud", this.result.application.id, "roles", 
          
          (paramUser.getRegistrationForApplication(this.result.application.id)).roles });
    if (uUID != null)
      map.put("sid", uUID); 
    JWTService.JWTResult jWTResult = this.jwtService.createJWT(getTenant(), paramUser, AuthenticationType.REGISTRATION, this.result.application, (Map)map, this.result.application.lambdaConfiguration.accessTokenPopulateId, null, JWTType.AccessToken, 






        
        Collections.emptySet(), null);
    this.response.token = jWTResult.encodedJWT;
    this.response.tokenExpirationInstant = jWTResult.jwt.expiration;
    if (this.result.application.loginConfiguration.generateRefreshTokens) {
      Map<String, Object> map1 = Map.of("amr", List.of("none"), "auth_time", 
          Long.valueOf(ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()), "source", "api");
      RefreshToken refreshToken = this.refreshTokenService.createRefreshToken(uUID, getTenant(), this.result.user, this.result.application, map1, new RefreshToken.MetaData());
      this.response.refreshToken = refreshToken.token;
      this.response.refreshTokenId = refreshToken.id;
    } 
  }
  
  private void normalizeRequestUser() {
    if (this.request.user == null)
      return; 
    this.request.user.id = this.userId;
    this.request.user.normalize();
    if (this.frontEndSupport.configuration.ignoreIdentitiesInUserAPIRequests())
      this.request.user.identities.clear(); 
  }
}
