package io.fusionauth.app.action.api.user;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.util.StringTools;
import io.fusionauth.api.service.mfa.MFAService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.TwoFactorDisableRequest;
import io.fusionauth.domain.api.TwoFactorRequest;
import io.fusionauth.domain.api.TwoFactorResponse;
import io.fusionauth.domain.api.TwoFactorUpdateRequest;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.jwt.domain.JWT;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.security.annotation.JWTAuthorizeMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{userId}", requiresAuthentication = true, scheme = {"api", "scoped-jwt"})
public class TwoFactorAction extends BaseTenantAPIAction {
  @JSONRequest(httpMethods = {"DELETE"})
  public final TwoFactorDisableRequest deleteRequest = new TwoFactorDisableRequest();
  
  @JSONRequest(httpMethods = {"POST"})
  public final TwoFactorRequest request = new TwoFactorRequest();
  
  @JSONRequest(httpMethods = {"PUT"})
  public final TwoFactorUpdateRequest updateRequest = new TwoFactorUpdateRequest();
  
  private final ReactorStatusService reactorStatusService;
  
  private final UserService userService;
  
  @JSONResponse
  public TwoFactorResponse response;
  
  public UUID userId;
  
  private UserService.TwoFactorEnableValidationResult result;
  
  @Inject
  public TwoFactorAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, ReactorStatusService paramReactorStatusService, UserService paramUserService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.reactorStatusService = paramReactorStatusService;
    this.userService = paramUserService;
  }
  
  @JWTAuthorizeMethod
  public boolean authorizeJWT(JWT paramJWT) {
    this.userId = StringTools.parseUUID(paramJWT.subject);
    return (this.userId != null);
  }
  
  public String delete() {
    if (this.result.user == null)
      return "missing"; 
    if (!this.userService.disableTwoFactor(getTenant(), this.result.application, this.result.user, this.result.method, this.deleteRequest.code, this.result.twoFactorMethodId, this.deleteRequest.eventInfo))
      return "invalid-two-factor-code"; 
    return "success";
  }
  
  public String post() {
    if (this.result.user == null)
      return "missing"; 
    UserService.TwoFactorEnableResult twoFactorEnableResult = this.userService.enableTwoFactor(getTenant(), this.result.application, this.result.user, this.request.code, this.request.secretBase32Encoded, this.request.email, this.result.method, this.request.mobilePhone, this.request.secret, this.request.twoFactorId, this.request.eventInfo, this.request.name);
    if (!twoFactorEnableResult.success)
      return "invalid-two-factor-code"; 
    this


      
      .response = (new TwoFactorResponse()).with(paramTwoFactorResponse -> paramTwoFactorResponse.code = paramTwoFactorEnableResult.twoFactorCode).with(paramTwoFactorResponse -> paramTwoFactorResponse.recoveryCodes = (this.result.user.twoFactor.methods.size() == 1) ? paramTwoFactorEnableResult.recoveryCodes : null);
    return "render";
  }
  
  public String put() {
    if (this.result.user == null)
      return "missing"; 
    this.userService.updateTwoFactor(this.result.user, this.result.twoFactorMethodId, this.updateRequest.name);
    return "success";
  }
  
  public void setApplicationId(UUID paramUUID) {
    this.deleteRequest.applicationId = paramUUID;
  }
  
  public void setCode(String paramString) {
    this.deleteRequest.code = paramString;
  }
  
  public void setMethodId(String paramString) {
    this.deleteRequest.methodId = paramString;
    this.updateRequest.methodId = paramString;
  }
  
  @ValidationMethod(httpMethods = {"DELETE"})
  public void validateDelete() {
    this.result = this.userService.validateDisableTwoFactor(getOptionalTenant(), this.deleteRequest.applicationId, this.userId, this.deleteRequest.code, this.deleteRequest.methodId);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (MFAService.LicensedMethods.contains(this.request.method) && ReactorStatusValidator.isNotLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.advancedMultiFactorAuthentication)) {
      this.frontEndSupport.addGeneralError("[notLicensed]", new Object[0]);
      return;
    } 
    this.result = this.userService.validateEnableTwoFactor(getOptionalTenant(), this.request.applicationId, this.userId, this.request.code, this.request.email, this.request.method, this.request.mobilePhone, this.request.secret, this.request.secretBase32Encoded, this.request.name);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
  
  @ValidationMethod(httpMethods = {"PUT"})
  public void validatePut() {
    this.result = this.userService.validateTwoFactorUpdate(getOptionalTenant(), this.userId, this.updateRequest.methodId, this.updateRequest.name);
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
