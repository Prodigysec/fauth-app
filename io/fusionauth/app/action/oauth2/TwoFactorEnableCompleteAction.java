package io.fusionauth.app.action.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.primeframework.PostAuthenticationStep;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import java.util.List;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class TwoFactorEnableCompleteAction extends BaseOAuthCompletionAction {
  @FTLVariable
  public List<String> recoveryCodes;
  
  @Inject
  public TwoFactorEnableCompleteAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    this.recoveryCodes = this.oauth_context.twoFactorRecoveryCodes;
    return "input";
  }
  
  public String post() {
    if (this.codeUser.passwordChangeRequired) {
      deleteLoginIntent();
      buildRedirectToChangePasswordURI(this.oauth_context.changePasswordId, this.oauth_context.changePasswordReason);
      return "redirect-to-change-password";
    } 
    String str = validateAndHandleErrors(false);
    if (str != null)
      return str; 
    LoginIntentService.LoginIntent loginIntent = getNextIntent(this.codeUser, this.codeUser.getRegistrationForApplication(this.codeApplication.id), PostAuthenticationStep.Authentication);
    if (loginIntent.step == PostAuthenticationStep.NotRegistered)
      deleteLoginIntent(); 
    return loginIntent.step.getResultCode();
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validate() {
    String str = validateAndHandleErrors(false);
    if (str != null)
      throw new ErrorException(str, false); 
    if (this.codeUser == null) {
      deleteLoginIntent();
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize");
    } 
    if (getPostAuthenticationStep() != this.codeLoginIntent.step)
      throw new ErrorException(handleRedirectToExpectedStep(this.codeLoginIntent)); 
    if (this.oauth_context == null) {
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize");
    } 
  }
  
  @JsonIgnore
  protected PostAuthenticationStep getPostAuthenticationStep() {
    return PostAuthenticationStep.Authentication;
  }
}
