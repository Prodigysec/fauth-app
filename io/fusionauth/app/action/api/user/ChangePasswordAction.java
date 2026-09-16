package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.error.Error;
import com.inversoft.error.Errors;
import com.inversoft.util.StringTools;
import io.fusionauth.api.domain.ChangePasswordResult;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.jwt.RefreshTokenService;
import io.fusionauth.api.service.mfa.MFAService;
import io.fusionauth.api.service.risk.CompositeRisk;
import io.fusionauth.api.service.risk.RiskSignalContext;
import io.fusionauth.api.service.risk.RiskSignalService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.PasswordService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.MultiFactorAction;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.user.ChangePasswordRequest;
import io.fusionauth.domain.api.user.ChangePasswordResponse;
import io.fusionauth.domain.jwt.RefreshToken;
import io.fusionauth.http.Cookie;
import io.fusionauth.jwt.domain.JWT;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.security.JWTRequestAdapter;
import org.primeframework.mvc.security.annotation.AuthorizeMethod;
import org.primeframework.mvc.security.annotation.JWTAuthorizeMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{changePasswordId}", requiresAuthentication = true, scheme = {"api", "scoped-jwt", "authorize-method"})
public class ChangePasswordAction extends BaseTenantAPIAction {
  @JSONRequest
  public final ChangePasswordRequest request = new ChangePasswordRequest();
  
  private final ExternalIdentifierReaderService externalIdentifierReader;
  
  private final JWTRequestAdapter jwtRequestAdapter;
  
  private final MFAService mfaService;
  
  private final PasswordService passwordService;
  
  private final RefreshTokenService refreshTokenService;
  
  private final RiskSignalService riskSignalService;
  
  private final UserService userService;
  
  public String changePasswordId;
  
  public String ipAddress;
  
  public String loginId;
  
  public List<String> loginIdTypes = new ArrayList<>();
  
  public RefreshToken.MetaData metaData;
  
  @JSONResponse
  public ChangePasswordResponse response;
  
  private ExternalIdentifierReaderService.ValidationResult changePasswordIdResult;
  
  private JWT jwt;
  
  private UserService.ValidationResult result;
  
  private UUID userId;
  
  @Inject
  public ChangePasswordAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ExternalIdentifierReaderService paramExternalIdentifierReaderService, JWTRequestAdapter paramJWTRequestAdapter, PasswordService paramPasswordService, RefreshTokenService paramRefreshTokenService, RiskSignalService paramRiskSignalService, UserService paramUserService, MFAService paramMFAService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.jwtRequestAdapter = paramJWTRequestAdapter;
    this.passwordService = paramPasswordService;
    this.refreshTokenService = paramRefreshTokenService;
    this.riskSignalService = paramRiskSignalService;
    this.userService = paramUserService;
    this.mfaService = paramMFAService;
  }
  
  @AuthorizeMethod(httpMethods = {"GET"})
  public boolean authorizeGET() {
    if (this.changePasswordId != null)
      return true; 
    if (this.jwtRequestAdapter.requestContainsJWT())
      return false; 
    return (this.loginId == null);
  }
  
  @JWTAuthorizeMethod(httpMethods = {"GET", "POST"})
  public boolean authorizeJWT(JWT paramJWT) {
    this.jwt = paramJWT;
    this.userId = StringTools.parseUUID(paramJWT.subject);
    return (this.userId != null);
  }
  
  @AuthorizeMethod(httpMethods = {"POST"})
  public boolean authorizePOST() {
    if (this.request.changePasswordId != null)
      return true; 
    if (this.jwtRequestAdapter.requestContainsJWT())
      return false; 
    return (this.request.loginId == null);
  }
  
  public String get() {
    if (this.changePasswordId != null && this.changePasswordIdResult.id == null)
      return "missing"; 
    if (this.result == null || this.result.user == null)
      return "missing"; 
    EventInfo eventInfo = this.frontEndSupport.buildEventInfo(this.metaData);
    if (this.ipAddress != null)
      eventInfo.ipAddress = this.ipAddress; 
    Tenant tenant = getTenant();
    RiskSignalContext riskSignalContext = new RiskSignalContext(this.result.user, this.ipAddress, eventInfo.userAgent, null, null, false, tenant.clientRiskConfiguration);
    CompositeRisk compositeRisk = this.riskSignalService.computeClientRisk(riskSignalContext);
    MFAService.ChallengeResult challengeResult = this.mfaService.determineChallengeRequired(MultiFactorAction.changePassword, tenant, this.result.user, this.result.application, null, eventInfo, compositeRisk, this.jwtRequestAdapter


        
        .getEncodedJWT(), null, true);
    if (challengeResult.required() && this.result.user.twoFactorEnabled()) {
      if (this.changePasswordIdResult != null && this.changePasswordIdResult.id != null && this.changePasswordIdResult.id.getAttributeAsBoolean("implicitTrust"))
        return "success"; 
      this.frontEndSupport.addGeneralError("[TrustTokenRequired]", new Object[0]);
      return "input";
    } 
    return "success";
  }
  
  public String post() {
    ChangePasswordResult changePasswordResult;
    if (this.result == null || this.result.user == null)
      return "missing"; 
    if (this.userId != null) {
      changePasswordResult = this.userService.updatePassword(getTenant(), this.result.application, this.request.password, this.request.currentPassword, this.jwt, this.result.refreshToken, this.request.trustToken, this.request.eventInfo, this.result.user);
    } else if (this.request.changePasswordId == null) {
      changePasswordResult = this.userService.updatePasswordByLoginId(getTenant(), this.result.application, this.request.loginId, this.request.password, this.request.currentPassword, this.request.trustToken, this.request.eventInfo, this.result.user);
    } else {
      changePasswordResult = this.userService.updatePasswordByChangePasswordId(getTenant(), this.result.application, this.result.user, this.changePasswordIdResult.id, this.request.password, this.request.currentPassword, this.request.trustToken, this.request.eventInfo);
    } 
    if (changePasswordResult.success && changePasswordResult.oneTimePassword != null) {
      this.response = new ChangePasswordResponse(changePasswordResult.oneTimePassword, changePasswordResult.state);
      return "render";
    } 
    if (changePasswordResult.success)
      return "success"; 
    if (this.userId == null && this.request.loginId != null && this.request.changePasswordId == null) {
      this.frontEndSupport.addFieldError("currentPassword", "[invalid]currentPassword", new Object[0]);
      return "input";
    } 
    return "missing";
  }
  
  @PostParameterMethod
  public void postParameter() {
    if (this.frontEndSupport.isPOST())
      if (this.request.changePasswordId == null)
        this.request.changePasswordId = this.changePasswordId;  
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    if (this.userId != null) {
      this.result = this.userService.validateId(getOptionalTenant(), null, this.userId);
      Cookie cookie = this.frontEndSupport.getCookie("refresh_token");
      if (cookie != null)
        this.request.refreshToken = cookie.value; 
      RefreshTokenService.RefreshTokenResult refreshTokenResult = this.refreshTokenService.retrieveRefreshTokenForUser(this.result.tenant, this.result.user, this.request.refreshToken);
      this.result.refreshToken = refreshTokenResult.refreshToken;
      this.result.application = refreshTokenResult.application;
    } else if (this.request.changePasswordId != null) {
      this.changePasswordIdResult = this.externalIdentifierReader.validate(getOptionalTenant(), this.request.changePasswordId, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.SetupPassword, ExternalIdentifier.ExternalIdType.ChangePassword });
      conditionallyUpdateTenant(this.changePasswordIdResult.tenant);
      if (this.changePasswordIdResult.id != null) {
        UUID uUID = (this.changePasswordIdResult.application != null) ? null : this.request.applicationId;
        this.result = this.userService.validateId(getTenant(), uUID, this.changePasswordIdResult.id.userId);
        this.result.application = (this.changePasswordIdResult.application != null) ? this.changePasswordIdResult.application : this.result.application;
      } 
    } else if (this.request.loginId != null) {
      this.result = this.userService.validateLoginId(getOptionalTenant(), this.request.applicationId, this.request.loginId, this.request.loginIdTypes);
      conditionallyUpdateTenant(this.result.tenant);
      assertTenantResolved();
      if (!this.result.errors.empty()) {
        this.frontEndSupport.transfer(this.result.errors);
        return;
      } 
      if (this.result.user == null) {
        this.frontEndSupport.addFieldError("loginId", "[invalid]loginId", new Object[] { this.request.loginId });
        return;
      } 
    } else {
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]);
      return;
    } 
    Errors errors = this.userService.validateChangePassword(this.userId, this.request.password, this.request.currentPassword);
    this.frontEndSupport.transfer(errors);
    if (this.result != null && this.result.user != null) {
      conditionallyUpdateTenant(this.result.tenant);
      errors = this.passwordService.validatePasswordForChangePasswordAction(getTenant(), this.result.application, this.result.user, this.result.user, "password", this.request.password, this.changePasswordIdResult, this.request.trustToken, this.request.trustChallenge, this.request.eventInfo, this.jwtRequestAdapter
          
          .getEncodedJWT());
      this.frontEndSupport.transfer(errors);
      if (errors.generalErrors.stream().anyMatch(paramError -> paramError.code.equals("[TrustTokenRequired]"))) {
        errors.fieldErrors.clear();
        throw new ErrorException("input", false);
      } 
    } 
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.userId != null) {
      this.result = this.userService.validateId(getOptionalTenant(), null, this.userId);
    } else if (this.changePasswordId != null) {
      this.changePasswordIdResult = this.externalIdentifierReader.validate(getOptionalTenant(), this.changePasswordId, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.SetupPassword, ExternalIdentifier.ExternalIdType.ChangePassword });
      conditionallyUpdateTenant(this.changePasswordIdResult.tenant);
      if (this.changePasswordIdResult.id != null) {
        UUID uUID = (this.changePasswordIdResult.application != null) ? null : this.request.applicationId;
        this.result = this.userService.validateId(getTenant(), uUID, this.changePasswordIdResult.id.userId);
        this.result.application = (this.changePasswordIdResult.application != null) ? this.changePasswordIdResult.application : this.result.application;
      } 
    } else if (this.loginId != null) {
      this.result = this.userService.validateLoginId(getOptionalTenant(), null, this.loginId, this.loginIdTypes);
      if (!this.result.errors.empty()) {
        this.frontEndSupport.transfer(this.result.errors);
        return;
      } 
    } 
    if (this.changePasswordId == null && (this.result == null || this.result.user == null) && this.loginId == null)
      this.frontEndSupport.addGeneralError("[invalidGET]", new Object[0]); 
  }
}
