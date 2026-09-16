package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ConfirmationRequiredReason;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.system.TenantService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.util.PhoneNumberTools;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.api.passwordless.PasswordlessLoginRequest;
import io.fusionauth.domain.api.passwordless.PasswordlessSendRequest;
import io.fusionauth.domain.api.passwordless.PasswordlessStartRequest;
import io.fusionauth.domain.api.passwordless.PasswordlessStartResponse;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.parameter.annotation.PreRenderMethod;
import org.primeframework.mvc.validation.ValidationMethod;

@Action("{code}")
@Forward(code = "browser-check", page = "/oauth2/passwordless/browser-check.ftl")
public class PasswordlessAction extends BaseOAuthAuthenticationAction {
  private final ExternalIdentifierService externalIdentifierService;
  
  public boolean browserCheck;
  
  public String code;
  
  public boolean formField;
  
  public String oneTimeCode;
  
  public boolean rememberDevice = true;
  
  @Inject
  public PasswordlessAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, ExternalIdentifierService paramExternalIdentifierService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.externalIdentifierService = paramExternalIdentifierService;
  }
  
  public String get() {
    if (this.code == null)
      return "input"; 
    if (requireUserConfirmation())
      return redirectToConfirmationRequired(ConfirmationRequiredReason.passwordlessLogin); 
    if (!this.browserCheck)
      return "browser-check"; 
    return completeLogin();
  }
  
  @PostParameterMethod
  public void initialize() {
    persistRememberDeviceChoice(this.rememberDevice, this.frontEndSupport.isPOST());
  }
  
  public String post() {
    if (this.oneTimeCode != null) {
      if (!this.browserCheck)
        return "browser-check"; 
      return completeLogin();
    } 
    ClientResponse<PasswordlessStartResponse, Errors> clientResponse = this.client.startPasswordlessLogin(new PasswordlessStartRequest(this.codeApplication.id, this.loginId, StandardLoginIdTypes, 

          
          captureState("rememberDevice", String.valueOf(this.rememberDevice))));
    if (clientResponse.wasSuccessful()) {
      allowConfirmationBypass();
      ClientResponse<Void, Errors> clientResponse1 = this.client.sendPasswordlessCode(new PasswordlessSendRequest(((PasswordlessStartResponse)clientResponse.successResponse).code));
      if (clientResponse1.wasSuccessful()) {
        if (((PasswordlessStartResponse)clientResponse.successResponse).oneTimeCode != null) {
          this.code = ((PasswordlessStartResponse)clientResponse.successResponse).code;
          this.formField = true;
          this.captcha_token = null;
          return passwordlessMessageSent();
        } 
        return passwordlessMessageSent();
      } 
      if (((Errors)clientResponse1.errorResponse).containsError("[missingEmail]"))
        addGeneralError("[MissingEmailAddressException]", new Object[0]); 
      if (((Errors)clientResponse1.errorResponse).containsError("[MessengerError]"))
        addGeneralError("[MessengerError]", new Object[0]); 
    } else {
      if (clientResponse.status == 404) {
        if (PhoneNumberTools.safeToE164format(this.loginId) != null)
          return completeFakePhoneRedirect(); 
        return passwordlessMessageSent();
      } 
      if (clientResponse.status == 400 && clientResponse.errorResponse != null && ((Errors)clientResponse.errorResponse).containsError("[disabled]"))
        addGeneralError("[PasswordlessDisabled]", new Object[0]); 
    } 
    return "input";
  }
  
  @PreRenderMethod({ThemedForward.class})
  public void prePageRenderSetup() {
    this.rememberDevice = getRememberDeviceCookieValue(this.rememberDevice, (this.frontEndSupport.isGET() && this.code == null));
    if (this.formField) {
      this.showCaptcha = false;
    } else {
      this.showCaptcha = (this.showCaptcha || showCaptchaOnInitialPageRender(null));
    } 
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validate() {
    String str = validateAndHandleErrors(true);
    if (str != null)
      throw new ErrorException(str, false); 
    this.scope = (this.scope == null) ? null : String.join(" ", (Iterable)this.scopes);
    PasswordlessAction passwordlessAction = this;
    (new Validator()).ifFalse(this.formField, paramValidator -> paramValidator.notBlank(this.loginId, "loginId", new Object[0])).ifTrue(this.formField, paramValidator -> paramValidator.notBlank(this.oneTimeCode, "oneTimeCode", new Object[0])).done(paramErrors -> paramPasswordlessAction.transferErrors(paramErrors));
    validateCaptchaAfterAttemptingToResolveUser(this.captcha_token, this.loginId, null, () -> this.showCaptcha = true);
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.code != null)
      return; 
    String str = validateAndHandleErrors(true);
    if (str != null)
      throw new ErrorException(str, false); 
  }
  
  protected String authenticateAuthorizationGrant(PasswordlessLoginRequest paramPasswordlessLoginRequest) {
    paramPasswordlessLoginRequest.noJWT = true;
    paramPasswordlessLoginRequest.eventInfo = this.frontEndSupport.buildEventInfo(this.metaData);
    paramPasswordlessLoginRequest.twoFactorTrustId = this.twoFactorTrustCookie.value;
    UUID uUID = resolveUserId(paramPasswordlessLoginRequest);
    if (this.ssoSession.user != null && !this.ssoSession.user.id.equals(uUID))
      this.ssoService.logout(this.ssoSession, this.ssoCookie, this.codeTenant, this.frontEndSupport.buildEventInfo(this.metaData)); 
    SSOService.NewDeviceResult newDeviceResult = handleNewDevice(paramPasswordlessLoginRequest, uUID);
    ClientResponse<LoginResponse, Errors> clientResponse = this.client.passwordlessLogin(paramPasswordlessLoginRequest);
    if (clientResponse.wasSuccessful())
      if (((LoginResponse)clientResponse.successResponse).state != null)
        unpackSavedState(((LoginResponse)clientResponse.successResponse).state);  
    OAuthService.OAuthValidationResult oAuthValidationResult = this.oauthService.validateAuthorizeRequest(TenantService.optionalTenantId(this.codeTenant), this.client_id, this.prompt, this.redirect_uri, this.response_mode, this.response_type, this.state, this.scope, this.code_challenge, this.code_challenge_method, true, this.resource);
    if (oAuthValidationResult.error != null) {
      if (oAuthValidationResult.doNotRedirect) {
        this.oauthJSONError = this.frontEndSupport.writeToPrettyString(oAuthValidationResult.error);
        return "render-error";
      } 
      buildAuthorizedRedirectWithError(oAuthValidationResult);
      return "authorized-redirect-with-error";
    } 
    setResultValues(oAuthValidationResult);
    if (this.formField && (clientResponse.status == 404 || clientResponse.status == 400)) {
      addFieldError("oneTimeCode", "[invalid]oneTimeCode", new Object[0]);
      return "input";
    } 
    if (clientResponse.status == 404) {
      addGeneralError("[InvalidPasswordlessLoginId]", new Object[0]);
      return "input";
    } 
    PasswordlessAction passwordlessAction = this;
    return handleInteractiveLoginResponse(clientResponse, newDeviceResult, paramErrors -> paramPasswordlessAction.transferErrors(paramErrors), this.rememberDevice ? SSOService.RememberDeviceState.Remember : SSOService.RememberDeviceState.Forget, AuthenticationType.PASSWORDLESS, false);
  }
  
  private String completeFakePhoneRedirect() {
    this.code = this.externalIdentifierService.generateExternalId(this.codeTenant, ExternalIdentifier.ExternalIdType.PasswordlessLogin);
    allowConfirmationBypass();
    this.formField = true;
    return passwordlessMessageSent();
  }
  
  private String completeLogin() {
    PasswordlessLoginRequest passwordlessLoginRequest = new PasswordlessLoginRequest(this.frontEndSupport.buildEventInfo(this.metaData), this.code);
    passwordlessLoginRequest.oneTimeCode = this.oneTimeCode;
    String str = authenticateAuthorizationGrant(passwordlessLoginRequest);
    removeConfirmationBypass();
    return str;
  }
  
  private String passwordlessMessageSent() {
    addGeneralInfo("[PasswordlessRequestSent]", new Object[0]);
    this.loginId = null;
    return "input";
  }
}
