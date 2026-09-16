package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.rest.ClientResponse;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.authentication.AuthenticationType;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.util.TwoFactorTools;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.PostAuthenticationStep;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.security.TwoFactorFrontendService;
import io.fusionauth.client.FusionAuthClient;
import io.fusionauth.domain.api.LoginResponse;
import io.fusionauth.domain.api.TwoFactorRequest;
import io.fusionauth.domain.api.TwoFactorResponse;
import io.fusionauth.domain.api.twoFactor.SecretResponse;
import io.fusionauth.domain.api.twoFactor.TwoFactorLoginRequest;
import io.fusionauth.domain.message.MessageType;
import io.fusionauth.domain.oauth2.OAuthError;
import java.util.List;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PostValidationMethod;

@Action
public class TwoFactorEnableAction extends BaseOAuthAuthenticationAction {
  private final TwoFactorFrontendService twoFactorFrontendService;
  
  public String action;
  
  public List<String> availableMethods;
  
  @FTLVariable
  public String code;
  
  public String email;
  
  public MessageType messageType;
  
  public String method;
  
  public String mobilePhone;
  
  public List<String> phoneMessageTypes;
  
  @FTLVariable
  public String secret;
  
  @FTLVariable
  public String secretBase32Encoded;
  
  @FTLVariable
  public String twoFactorName;
  
  @Inject
  public TwoFactorEnableAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, TwoFactorFrontendService paramTwoFactorFrontendService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
    this.twoFactorFrontendService = paramTwoFactorFrontendService;
    this.errorMapping.put("name", "twoFactorName");
  }
  
  public String get() {
    SecretResponse secretResponse = this.delegate.execute(FusionAuthClient::generateTwoFactorSecret);
    this.secret = secretResponse.secret;
    this.secretBase32Encoded = secretResponse.secretBase32Encoded;
    if (this.email == null && this.codeUser.email != null)
      this.email = this.codeUser.email; 
    if (this.mobilePhone == null && this.codeUser.mobilePhone != null)
      this.mobilePhone = this.codeUser.mobilePhone; 
    return "input";
  }
  
  public String post() {
    if (this.action != null && this.action.equals("send")) {
      handleEnableSend(this.email, this.method, this.mobilePhone, this.messageType, this.codeUser.id);
      return "input";
    } 
    ClientResponse<TwoFactorResponse, Errors> clientResponse = this.client.enableTwoFactor(this.codeUser.id, (new TwoFactorRequest())
        
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.applicationId = (this.codeApplication != null) ? this.codeApplication.id : null)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.code = this.code)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.email = this.email)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.eventInfo = this.frontEndSupport.buildEventInfo(this.metaData))
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.method = this.method)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.mobilePhone = this.mobilePhone)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.name = this.twoFactorName)
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.secret = this.secret)
        
        .with(paramTwoFactorRequest -> paramTwoFactorRequest.twoFactorId = this.twoFactorId));
    if (clientResponse.wasSuccessful()) {
      addGeneralInfo("[TwoFactorAuthenticationMethodEnabled]", new Object[0]);
      ExternalIdentifierReaderService.ValidationResult validationResult = this.twoFactorFrontendService.verifyTwoFactorId(this.codeTenant, this.twoFactorId);
      AuthenticationType authenticationType = (validationResult.id != null && validationResult.id.getAttribute("authenticationType") != null) ? AuthenticationType.valueOf(validationResult.id.getAttribute("authenticationType")) : null;
      TwoFactorLoginRequest twoFactorLoginRequest = (new TwoFactorLoginRequest()).with(paramTwoFactorLoginRequest -> paramTwoFactorLoginRequest.applicationId = (this.codeApplication != null) ? this.codeApplication.id : null).with(paramTwoFactorLoginRequest -> paramTwoFactorLoginRequest.code = ((TwoFactorResponse)paramClientResponse.successResponse).code).with(paramTwoFactorLoginRequest -> paramTwoFactorLoginRequest.eventInfo = this.frontEndSupport.buildEventInfo(this.metaData)).with(paramTwoFactorLoginRequest -> paramTwoFactorLoginRequest.twoFactorId = this.twoFactorId);
      twoFactorLoginRequest.noJWT = true;
      twoFactorLoginRequest.trustComputer = false;
      ClientResponse<LoginResponse, Errors> clientResponse1 = this.client.twoFactorLogin(twoFactorLoginRequest);
      if (clientResponse1.exception != null) {
        addGeneralError("[APIError]", new Object[0]);
        return "render-error";
      } 
      if (clientResponse1.status == 404 || clientResponse1.status == 409) {
        addGeneralInfo("[TwoFactorTimeout]", new Object[0]);
        buildRedirectToAuthorizeURI();
        return "redirect-to-authorize";
      } 
      if (clientResponse1.status == 421) {
        addGeneralError("[TwoFactorEnableFailed]", new Object[0]);
        buildRedirectToAuthorizeURI();
        return "redirect-to-authorize";
      } 
      if (this.oauth_context == null)
        this.oauth_context = new BaseOAuthAction.OAuthContext(); 
      if (authenticationType == null)
        authenticationType = AuthenticationType.PASSWORD; 
      this.oauth_context.addAuthenticationType(authenticationType);
      this.oauth_context.changePasswordId = ((LoginResponse)clientResponse1.successResponse).changePasswordId;
      this.oauth_context.changePasswordReason = ((LoginResponse)clientResponse1.successResponse).changePasswordReason;
      this.oauth_context.emailVerificationId = ((LoginResponse)clientResponse1.successResponse).emailVerificationId;
      this.oauth_context.registrationVerificationId = ((LoginResponse)clientResponse1.successResponse).registrationVerificationId;
      this.loginIntentCookie.value = this.loginIntentService.buildLoginIntent(this.codeTenant, this.codeApplication.id, this.codeUser.id, PostAuthenticationStep.Authentication, 
          getRememberDeviceState(), 
          (this.oauth_context.registrationVerificationId != null) ? this.oauth_context.registrationVerificationId : this.oauth_context.emailVerificationId);
      List<String> list = ((TwoFactorResponse)clientResponse.successResponse).recoveryCodes;
      if (list != null && !list.isEmpty())
        this.oauth_context.twoFactorRecoveryCodes = list; 
      this
        .redirectToTwoFactorEnableCompleteURI = baseQueryBuilder("/oauth2/two-factor-enable-complete").build();
      return "redirect-to-two-factor-enable-complete";
    } 
    if (clientResponse.status == 421) {
      addFieldError("code", "[invalid]code", new Object[0]);
    } else {
      transferErrors((Errors)clientResponse.errorResponse);
    } 
    return "input";
  }
  
  @PostValidationMethod
  public void setup() {
    this.availableMethods = this.twoFactorFrontendService.availableMethodsToConfigureForUser(this.codeTenant, this.codeUser);
    this.phoneMessageTypes = TwoFactorTools.getPhoneMessageTypesForTenant(this.codeTenant);
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    if (this.client_id != null) {
      String str = validateAndHandleErrors(false, paramOAuthError -> !paramOAuthError.reason.name().startsWith("missing_"));
      if (str != null)
        throw new ErrorException(str, false); 
    } 
    if (this.twoFactorId == null) {
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize");
    } 
    if (this.action == null && this.frontEndSupport.isPOST()) {
      TwoFactorEnableAction twoFactorEnableAction = this;
      (new Validator()).notBlank(this.code, "code", new Object[0]).done(paramErrors -> paramTwoFactorEnableAction.transferErrors(paramErrors));
    } 
    ExternalIdentifierReaderService.ValidationResult validationResult = this.twoFactorFrontendService.verifyTwoFactorId(this.codeTenant, this.twoFactorId);
    if (validationResult == null || validationResult.user == null) {
      addGeneralInfo("[TwoFactorTimeout]", new Object[0]);
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize");
    } 
    setUserVariables(validationResult.user);
  }
}
