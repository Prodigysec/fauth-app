package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.security.TwoFactorFrontendService;
import io.fusionauth.domain.ChangePasswordReason;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.api.twoFactor.TwoFactorLoginRequest;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.http.Cookie;
import java.util.List;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.scope.annotation.ManagedCookie;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class TwoFactorAction extends BaseOAuthAuthenticationAction {
  private final TwoFactorFrontendService twoFactorFrontendService;
  
  public String code;
  
  @FTLVariable
  public TwoFactorMethod method;
  
  public String methodId;
  
  @Deprecated
  @FTLVariable
  public boolean pushEnabled;
  
  @Deprecated
  @FTLVariable
  public boolean pushPreferred;
  
  @Deprecated
  @FTLVariable
  public boolean resendCode;
  
  @FTLVariable
  public boolean showResendOrSelectMethod;
  
  public boolean trustComputer;
  
  @ManagedCookie(name = "fusionauth.trust_t")
  public Cookie trustToken;
  
  @Deprecated
  @FTLVariable
  public boolean userCanReceivePush;
  
  @Inject
  public TwoFactorAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, TwoFactorFrontendService paramTwoFactorFrontendService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.twoFactorFrontendService = paramTwoFactorFrontendService;
  }
  
  public String get() {
    return "input";
  }
  
  public String post() {
    TwoFactorLoginRequest twoFactorLoginRequest = new TwoFactorLoginRequest(this.frontEndSupport.buildEventInfo(this.metaData), (this.codeApplication != null) ? this.codeApplication.id : null, this.code, this.twoFactorId);
    twoFactorLoginRequest.noJWT = true;
    twoFactorLoginRequest.trustComputer = this.trustComputer;
    ClientResponse<LoginResponse, Errors> clientResponse = this.client.twoFactorLogin(twoFactorLoginRequest);
    if (clientResponse.status == 200 || clientResponse.status == 202 || clientResponse.status == 212 || clientResponse.status == 213) {
      if (this.trustComputer && ((LoginResponse)clientResponse.successResponse).twoFactorTrustId != null)
        this.twoFactorTrustCookie.value = ((LoginResponse)clientResponse.successResponse).twoFactorTrustId; 
      this.trustToken.setValue(((LoginResponse)clientResponse.successResponse).trustToken);
      if (((LoginResponse)clientResponse.successResponse).state != null && ((LoginResponse)clientResponse.successResponse).state.containsKey("changePasswordId")) {
        buildRedirectToChangePasswordURI((String)((LoginResponse)clientResponse.successResponse).state.get("changePasswordId"), (ChangePasswordReason)null);
        return "redirect-to-change-password";
      } 
      String str = validateAndHandleErrors(false);
      if (str != null)
        return str; 
      return (handlePostAuthenticationRedirect(((LoginResponse)clientResponse.successResponse).user, getRememberDeviceState())).step.getResultCode();
    } 
    if (clientResponse.exception != null) {
      addGeneralError("[APIError]", new Object[0]);
      return "render-error";
    } 
    if (clientResponse.status == 203) {
      if (this.trustComputer && ((LoginResponse)clientResponse.successResponse).twoFactorTrustId != null)
        this.twoFactorTrustCookie.value = ((LoginResponse)clientResponse.successResponse).twoFactorTrustId; 
      buildRedirectToChangePasswordURI(((LoginResponse)clientResponse.successResponse).changePasswordId, ((LoginResponse)clientResponse.successResponse).changePasswordReason);
      return "redirect-to-change-password";
    } 
    if (clientResponse.status == 400) {
      transferErrors((Errors)clientResponse.errorResponse);
    } else {
      if (clientResponse.status == 404) {
        addGeneralInfo("[TwoFactorTimeout]", new Object[0]);
        buildRedirectToAuthorizeURI();
        return "redirect-to-authorize";
      } 
      if (clientResponse.status == 409) {
        addGeneralError("[LoginPreventedExceptionTooManyTwoFactorAttempts]", new Object[0]);
        buildRedirectToAuthorizeURI();
        return "redirect-to-authorize";
      } 
      if (clientResponse.status == 504) {
        addGeneralError("[WebhookTransactionException]", new Object[0]);
      } else {
        addFieldError("code", "[invalid]code", new Object[0]);
      } 
    } 
    return "input";
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    if (this.client_id != null || this.twoFactorId == null) {
      String str = validateAndHandleErrors(false, paramOAuthError -> (this.twoFactorId == null || !paramOAuthError.reason.name().startsWith("missing_")));
      if (str != null)
        throw new ErrorException(str, false); 
    } 
    if (this.twoFactorId == null) {
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize");
    } 
    if (this.frontEndSupport.isPOST()) {
      TwoFactorAction twoFactorAction = this;
      (new Validator()).notBlank(this.code, "code", new Object[0]).done(paramErrors -> paramTwoFactorAction.transferErrors(paramErrors));
    } 
    ExternalIdentifierReaderService.ValidationResult validationResult = this.twoFactorFrontendService.verifyTwoFactorId(this.codeTenant, this.twoFactorId);
    if (validationResult == null || validationResult.user == null) {
      addGeneralInfo("[TwoFactorTimeout]", new Object[0]);
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize");
    } 
    if (this.frontEndSupport.isGET() && this.twoFactorTrustCookie.value != null) {
      ExternalIdentifier externalIdentifier = this.frontEndSupport.getExternalId(this.codeTenant.id, this.twoFactorTrustCookie.value, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.TwoFactorTrust });
      this.trustComputer = (externalIdentifier != null && validationResult.user.id.equals(externalIdentifier.userId));
    } 
    this.method = validationResult.user.twoFactor.getMethodById(this.methodId);
    if (this.method != null)
      this.method.secure(); 
    List<String> list = this.twoFactorFrontendService.availableMethodsToUseAtLogin(this.codeTenant, validationResult.user);
    this.pushEnabled = list.contains("sms");
    this.userCanReceivePush = this.pushEnabled;
    this.pushPreferred = this.userCanReceivePush;
    this.showResendOrSelectMethod = (list.contains("email") || list.contains("sms"));
  }
}
