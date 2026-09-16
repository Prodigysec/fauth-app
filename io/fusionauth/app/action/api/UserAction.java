package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityExternalIdHelper;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.jwt.JWTService;
import io.fusionauth.api.service.jwt.claims.JWTType;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.IdentityTypeHelper;
import io.fusionauth.api.service.user.IdentityTypeValidator;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.api.service.user.VerificationIdResponseHelper;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.api.UserDeleteSingleRequest;
import io.fusionauth.domain.api.UserRequest;
import io.fusionauth.domain.api.UserResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONPatch;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PreParameter;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userId}", requiresAuthentication = true, scheme = {"api"})
public class UserAction extends BaseTenantAPIAction implements Patchable {
  @JSONRequest(httpMethods = {"DELETE"})
  public final UserDeleteSingleRequest deleteRequest = new UserDeleteSingleRequest();
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final JWTService jwtService;
  
  private final UserReaderService userReader;
  
  private final UserService userService;
  
  public String changePasswordId;
  
  public String email;
  
  public String loginId;
  
  public List<String> loginIdTypes = new ArrayList<>();
  
  public boolean reactivate;
  
  @JSONPatch
  @JSONRequest(httpMethods = {"GET", "PATCH", "POST", "PUT"})
  public UserRequest request = new UserRequest();
  
  @JSONResponse
  public UserResponse response;
  
  @PreParameter
  public UUID userId;
  
  public String username;
  
  public String verificationId;
  
  private UserService.ValidationResult result;
  
  @Inject
  public UserAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ExternalIdentifierReaderService paramExternalIdentifierReaderService, JWTService paramJWTService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.jwtService = paramJWTService;
    this.userReader = paramUserReaderService;
    this.userService = paramUserService;
  }
  
  public String delete() {
    if (this.result.existing == null)
      return "missing"; 
    if (this.deleteRequest.hardDelete) {
      this.userService.delete(getTenant(), this.result.existing, this.deleteRequest.eventInfo);
    } else {
      this.userService.deactivate(getTenant(), this.result.existing, this.deleteRequest.eventInfo);
    } 
    return "success";
  }
  
  public String get() {
    if (this.result == null || this.result.user == null)
      return "missing"; 
    String str = this.result.user.encryptionScheme;
    Integer integer = this.result.user.factor;
    this.response = new UserResponse(this.result.user.secure().sort());
    this.response.user.encryptionScheme = str;
    this.response.user.factor = integer;
    this.response.verificationIds = VerificationIdResponseHelper.getSortedVerificationIds(this.result.verificationIds, this::addLegacyEmailVerificationIdsToResponse);
    if (this.result.registrationVerificationIds != null && !this.result.registrationVerificationIds.isEmpty())
      this.response.registrationVerificationIds = this.result.registrationVerificationIds; 
    if (this.result.registrationVerificationOneTimeCodes != null && !this.result.registrationVerificationOneTimeCodes.isEmpty())
      this.response.registrationVerificationOneTimeCodes = this.result.registrationVerificationOneTimeCodes; 
    return "render";
  }
  
  public void loadExisting() {
    if (this.userId != null) {
      this.request.user = this.userReader.retrieveById(getOptionalTenantId(), this.userId);
      if (this.request.user != null)
        this.request.user.secure().sort(); 
    } 
  }
  
  public String post() {
    User user = this.result.user;
    UserService.UserResult userResult = this.userService.create(getTenant(), this.result.application, user, this.result.sendSetPasswordIdentityType, this.request.skipVerification, true, true, true, this.request.eventInfo, this.result.preVerifiedExternalIds);
    this.response = new UserResponse(user.secure().sort());
    this.response.verificationIds = VerificationIdResponseHelper.getSortedVerificationIds(userResult.verificationIds, this::addLegacyEmailVerificationIdsToResponse);
    if (this.result.sendSetPasswordIdentityType == SendSetPasswordIdentityType.doNotSend)
      addTokenToResponse(user, Map.of("amr", List.of("none"))); 
    return "render";
  }
  
  public String put() {
    if (this.result.existing == null)
      return "missing"; 
    if (this.reactivate) {
      this.userService.reactivate(getTenant(), this.result.existing, this.request.eventInfo);
      this.response = new UserResponse(this.result.existing.secure().sort());
    } else {
      UserService.UserResult userResult = this.userService.update(getTenant(), this.result.application, this.result.existing, this.result.user, this.request.skipVerification, this.request.passwordFieldType, this.request.eventInfo);
      this.response = new UserResponse(this.result.user.secure().sort());
      this.response.verificationIds = VerificationIdResponseHelper.getSortedVerificationIds(userResult.verificationIds, this::addLegacyEmailVerificationIdsToResponse);
    } 
    return "render";
  }
  
  public void setHardDelete(boolean paramBoolean) {
    this.deleteRequest.hardDelete = paramBoolean;
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    this.result = this.userService.validateUserId(getOptionalTenant(), this.userId);
    this.frontEndSupport.transfer(this.result.errors);
    conditionallyUpdateTenant(this.result.tenant);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.userId != null) {
      this.result = this.userService.validateRetrieveById(getOptionalTenant(), this.userId);
    } else if (this.email != null) {
      this.result = this.userService.validateRetrieveByLoginId(getTenant(), this.email, List.of(IdentityType.email));
    } else if (this.username != null) {
      this.result = this.userService.validateRetrieveByLoginId(getTenant(), this.username, List.of(IdentityType.username));
    } else if (this.loginId != null) {
      Objects.requireNonNull(this.frontEndSupport);
      (new Validator()).validate(paramValidator -> IdentityTypeValidator.validate(paramValidator, this.loginIdTypes, "loginIdTypes")).ifNoErrors(() -> {
            List<IdentityType> list = IdentityTypeHelper.convert(this.loginIdTypes);
            this.result = this.userService.validateRetrieveByLoginId(getTenant(), this.loginId, list);
          }).done(this.frontEndSupport::transfer);
    } else if (this.verificationId != null) {
      ExternalIdentifierReaderService.ValidationResult validationResult = this.externalIdentifierReader.validate(getOptionalTenant(), this.verificationId, IdentityExternalIdHelper.identityExternalIdTypes);
      if (validationResult.id != null) {
        conditionallyUpdateTenant(validationResult.tenant);
        this.result = this.userService.validateRetrieveById(getTenant(), validationResult.id.userId);
      } 
    } else if (this.changePasswordId != null) {
      ExternalIdentifierReaderService.ValidationResult validationResult = this.externalIdentifierReader.validate(getOptionalTenant(), this.changePasswordId, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.ChangePassword, ExternalIdentifier.ExternalIdType.SetupPassword });
      if (validationResult.id != null) {
        conditionallyUpdateTenant(validationResult.tenant);
        this.result = this.userService.validateRetrieveById(getTenant(), validationResult.id.userId);
      } 
    } else {
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]);
    } 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    normalizeRequestUser();
    if (!this.request.verificationIds.isEmpty()) {
      this.result = this.userService.validatePreVerifiedCreate(getOptionalTenant(), this.request.user, this.request.applicationId, this.request.verificationIds, this.request.disableDomainBlock, this.request.sendSetPasswordIdentityType, this.request.sendSetPasswordEmail, this.request.eventInfo);
      conditionallyUpdateTenant(this.result.tenant);
    } else {
      if (this.request.user == null) {
        this.frontEndSupport.addFieldError("user", "[missing]user", new Object[0]);
        return;
      } 
      this.result = this.userService.validateCreate(getTenant(), this.request.user, this.request.applicationId, this.request.disableDomainBlock, this.request.sendSetPasswordIdentityType, this.request.sendSetPasswordEmail, this.request.eventInfo, false);
    } 
    this.frontEndSupport.transfer(this.result.errors, this.result.fieldMapping);
  }
  
  @ValidationMethod(httpMethods = {"PUT", "PATCH"})
  public void validatePutAndPatch() {
    if (this.userId == null) {
      this.frontEndSupport.addFieldError("userId", "[missing]userId", new Object[0]);
      return;
    } 
    Tenant tenant = getOptionalTenant();
    if (this.reactivate) {
      this.result = this.userService.validateUserId(tenant, this.userId);
      conditionallyUpdateTenant(this.result.tenant);
      this.frontEndSupport.transfer(this.result.errors);
      return;
    } 
    if (this.request.user == null) {
      this.frontEndSupport.addFieldError("user", "[missing]user", new Object[0]);
      return;
    } 
    this.request.user.id = this.userId;
    this.request.user.normalize();
    this.request.user.getRegistrations().clear();
    if (this.frontEndSupport.configuration.ignoreIdentitiesInUserAPIRequests())
      this.request.user.identities.clear(); 
    this.result = this.userService.validateUpdate(tenant, this.request.user, this.frontEndSupport.configuration
        
        .ignoreIdentitiesInUserAPIRequests(), this.request.applicationId, this.request.currentPassword, this.request.disableDomainBlock, this.request.eventInfo, this.request.passwordFieldType);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors, this.result.fieldMapping);
  }
  
  private void addLegacyEmailVerificationIdsToResponse(ExternalIdentifier paramExternalIdentifier, UserIdentity paramUserIdentity) {
    if (paramUserIdentity.type.is(IdentityType.email) && paramUserIdentity.primary) {
      this.response.emailVerificationId = paramExternalIdentifier.id;
      this.response.emailVerificationOneTimeCode = paramExternalIdentifier.getAttribute("otp");
    } 
  }
  
  private void addTokenToResponse(User paramUser, Map<String, Object> paramMap) {
    JWTService.JWTResult jWTResult = this.jwtService.createJWT(getTenant(), paramUser, AuthenticationType.USER_CREATE, null, paramMap, null, null, JWTType.AccessToken, 






        
        Collections.emptySet(), null);
    this.response.token = jWTResult.encodedJWT;
    this.response.tokenExpirationInstant = jWTResult.jwt.expiration;
  }
  
  private void normalizeRequestUser() {
    if (this.request.user == null)
      return; 
    this.request.user.id = this.userId;
    this.request.user.normalize();
    this.request.user.getRegistrations().clear();
    if (this.frontEndSupport.configuration.ignoreIdentitiesInUserAPIRequests())
      this.request.user.identities.clear(); 
  }
}
