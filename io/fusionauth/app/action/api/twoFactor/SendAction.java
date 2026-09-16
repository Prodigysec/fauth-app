package io.fusionauth.app.action.api.twoFactor;

import com.google.inject.Inject;
import com.inversoft.authentication.api.service.AuthenticationKeyCache;
import com.inversoft.util.StringTools;
import io.fusionauth.api.service.mfa.MFAService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.app.action.api.BaseTenantAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.twoFactor.TwoFactorSendRequest;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.jwt.domain.JWT;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.security.annotation.AuthorizeMethod;
import org.primeframework.mvc.security.annotation.JWTAuthorizeMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{twoFactorId}", requiresAuthentication = true, scheme = {"api", "scoped-jwt", "authorize-method"})
public class SendAction extends BaseTenantAPIAction {
  @JSONRequest
  public final TwoFactorSendRequest request = new TwoFactorSendRequest();
  
  private final MFAService mfaService;
  
  private final ReactorStatusService reactorStatusService;
  
  public String twoFactorId;
  
  private MFAService.ValidationResult result;
  
  @Inject
  public SendAction(FrontEndSupport paramFrontEndSupport, AuthenticationKeyCache paramAuthenticationKeyCache, MFAService paramMFAService, ReactorStatusService paramReactorStatusService) {
    super(paramFrontEndSupport, paramAuthenticationKeyCache);
    this.mfaService = paramMFAService;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  @AuthorizeMethod(httpMethods = {"POST"})
  public boolean authorize() {
    return (this.twoFactorId != null);
  }
  
  @JWTAuthorizeMethod(httpMethods = {"POST"})
  public boolean authorize(JWT paramJWT) {
    this.request.userId = StringTools.parseUUID(paramJWT.subject);
    return (this.request.userId != null);
  }
  
  public String post() {
    if (this.twoFactorId != null) {
      this.mfaService.sendTwoFactorCode(getTenant(), this.result.application, this.result.user, this.twoFactorId, this.result.methodId, this.result.messageType);
    } else {
      this.mfaService.sendTwoFactorCodeForEnableDisable(getTenant(), this.result.application, this.result.user, this.result.methodId, this.result.method, this.request.email, this.request.mobilePhone, this.result.messageType);
    } 
    return "success";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (ReactorStatusValidator.isNotLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.advancedMultiFactorAuthentication)) {
      this.frontEndSupport.addGeneralError("[notLicensed]", new Object[0]);
      return;
    } 
    if (this.request.method != null || this.request.userId != null) {
      this.result = this.mfaService.validateSendForEnableDisable(getOptionalTenant(), this.request.applicationId, this.request.userId, this.request.methodId, this.request.method, this.request.email, this.request.mobilePhone, this.request.messageType);
    } else {
      if (this.twoFactorId == null) {
        this.frontEndSupport.addFieldError("twoFactorId", "[missing]twoFactorId", new Object[0]);
        return;
      } 
      this.result = this.mfaService.validateSend(getOptionalTenant(), this.twoFactorId, this.request.methodId, this.request.messageType);
    } 
    conditionallyUpdateTenant(this.result.tenant);
    this.frontEndSupport.transfer(this.result.errors);
  }
}
