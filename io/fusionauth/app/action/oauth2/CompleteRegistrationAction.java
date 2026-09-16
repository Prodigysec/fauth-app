package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.api.util.OAuthTools;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.PostAuthenticationStep;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.app.service.user.RegistrationFrontendService;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.UserTwoFactorConfiguration;
import io.fusionauth.domain.oauth2.OAuthError;
import java.util.Collections;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.validation.ValidationMethod;
import org.primeframework.mvc.validation.annotation.PreValidationMethod;

@Action
public class CompleteRegistrationAction extends BaseRegisterAction {
  @Deprecated(since = "1.60.0")
  @FTLVariable
  public boolean passwordSet = true;
  
  private LoginIntentService.LoginIntent codeLoginIntent;
  
  private RegistrationFrontendService.RegistrationCompleteResult result;
  
  @Inject
  public CompleteRegistrationAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, RegistrationFrontendService paramRegistrationFrontendService, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramOAuthService, paramRegistrationFrontendService, paramLoginIntentService, paramSSOService, paramThreatDetectionService, paramUserReaderService, paramUserService);
  }
  
  public String get() {
    if (!this.result.registered && this.result.registrationComplete) {
      this.result.registration = (new UserRegistration()).with(paramUserRegistration -> paramUserRegistration.applicationId = this.codeApplication.id);
      this.userService.createRegistration(this.codeTenant, this.codeApplication, this.registrationState.user, this.result.registration, 
          Collections.emptyList(), false, false, false, this.frontEndSupport
          .buildEventInfo(null));
      this.result.registered = true;
    } 
    if (this.result.registered && this.result.registrationComplete) {
      User user = this.result.fullUser;
      LoginIntentService.LoginIntent loginIntent = getNextIntent(user, this.result.registration, PostAuthenticationStep.CompleteRegistration);
      this.loginIntentService.updatePostAuthenticationStep(this.codeLoginIntent, loginIntent.step, loginIntent.verificationId);
      return loginIntent.step.getResultCode();
    } 
    if (this.prompts.contains("none")) {
      buildAuthorizedRedirectWithError((new OAuthService.OAuthValidationResult()).withError(new OAuthError(OAuthError.OAuthErrorType.interaction_required, OAuthError.OAuthErrorReason.registration_missing_requirement, "The user is required to complete their registration in order to complete authentication.")));
      return "authorized-redirect-with-error";
    } 
    if (this.result.registered)
      addGeneralInfo("[AdditionalFieldsRequired]", new Object[0]); 
    return "input";
  }
  
  @PreValidationMethod
  public void initializeRegistrationState() {
    if (this.registrationState == null)
      this.registrationState = this.registrationFrontendService.registrationBegin(this.codeTenant, this.codeApplication, this.codeUser, true); 
  }
  
  public String post() {
    if (!this.registrationState.isLastStep()) {
      this.registrationState.nextStep();
      return "input";
    } 
    UserService.UserResult userResult = this.registrationFrontendService.updateUserForCompleteRegistration(this.codeTenant, this.codeApplication, this.registrationState.user, this.registrationState.registration, this.registrationState
        .getForm(), this.registrationState.consents, this.frontEndSupport
        .buildEventInfo(this.metaData));
    User user = userResult.user;
    UserRegistration userRegistration = (userResult.registration != null) ? userResult.registration : this.registrationState.registration;
    LoginIntentService.LoginIntent loginIntent = getNextIntent(user, userRegistration, PostAuthenticationStep.CompleteRegistration);
    this.loginIntentService.updatePostAuthenticationStep(this.codeLoginIntent, loginIntent.step, loginIntent.verificationId);
    return loginIntent.step.getResultCode();
  }
  
  public void resolveUserAndZoneId() {
    setUserVariables((this.codeLoginIntent != null) ? this.codeLoginIntent.user : null);
  }
  
  @PostParameterMethod
  public void retrieveLoginIntent() {
    this.codeLoginIntent = this.loginIntentService.getLoginIntent(this.codeTenant, this.codeApplication, this.loginIntentCookie);
    resolveUserAndZoneId();
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    commonValidation();
    this.result = this.registrationFrontendService.registrationComplete(this.codeTenant, this.codeApplication, this.codeUser.id, this.registrationState);
    this.registrationState.user = this.result.user;
    this.registrationState.registration = this.result.registration;
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    commonValidation();
    RegistrationFrontendService.ValidationResult validationResult = null;
    if (!this.registrationState.isBasicRegistration()) {
      validationResult = this.registrationFrontendService.normalizeAndValidateToCompleteRegistration(this.codeTenant, this.codeApplication, this.registrationState, this.confirm.user, this.confirm.registration, this.frontEndSupport
          
          .buildEventInfo(this.metaData));
      transferErrors(validationResult.errors);
    } 
    if (validationResult != null && !validationResult.errors.empty())
      return; 
    if (!this.registrationState.isLastStep())
      return; 
    clearUser();
    User user1 = this.codeUser;
    User user2 = this.registrationState.user;
    user2.id = user1.id;
    user2.connectorId = user1.connectorId;
    user2.expiry = user1.expiry;
    user2.imageUrl = user1.imageUrl;
    user2.lastLoginInstant = user1.lastLoginInstant;
    user2.parentEmail = user1.parentEmail;
    user2.password = null;
    user2.tenantId = user1.tenantId;
    user2.timezone = user1.timezone;
    user2.twoFactor = new UserTwoFactorConfiguration(user1.twoFactor);
    user2.normalize();
    normalizeAndValidateWithRegistrationConfiguration(user2, false, (String)null, false);
  }
  
  private void commonValidation() {
    String str = validateAndHandleErrors(false);
    if (str != null) {
      deleteLoginIntent();
      throw new ErrorException(str, false);
    } 
    if (!OAuthTools.isCompleteRegistrationAllowed(this.codeApplication, this.codeUser)) {
      deleteLoginIntent();
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize", false);
    } 
    if (PostAuthenticationStep.CompleteRegistration != this.codeLoginIntent.step)
      throw new ErrorException(handleRedirectToExpectedStep(this.codeLoginIntent)); 
  }
}
