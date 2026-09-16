package io.fusionauth.app.action.oauth2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.primeframework.PostAuthenticationStep;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.oauth2.OAuthError;
import java.util.Set;
import java.util.function.Predicate;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

public abstract class BaseOAuthCompletionAction extends BaseOAuthAction {
  protected LoginIntentService.LoginIntent codeLoginIntent;
  
  protected BaseOAuthCompletionAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  public void resolveUserAndZoneId() {
    setUserVariables((this.codeLoginIntent != null) ? this.codeLoginIntent.user : null);
  }
  
  @PostParameterMethod
  public void retrieveLoginIntent() {
    this.codeLoginIntent = this.loginIntentService.getLoginIntent(this.codeTenant, this.codeApplication, this.loginIntentCookie);
    resolveUserAndZoneId();
  }
  
  @JsonIgnore
  protected LoginIntentService.LoginIntent getNextIntent(User paramUser, UserRegistration paramUserRegistration, PostAuthenticationStep paramPostAuthenticationStep) {
    LoginIntentService.LoginIntent loginIntent = super.getNextIntent(paramUser, paramUserRegistration, paramPostAuthenticationStep);
    this.loginIntentService.updatePostAuthenticationStep(this.codeLoginIntent, loginIntent.step, loginIntent.verificationId);
    return loginIntent;
  }
  
  protected abstract PostAuthenticationStep getPostAuthenticationStep();
  
  protected String validateAndHandleErrors(boolean paramBoolean) {
    return validateAndHandleErrors(paramBoolean, (Predicate<OAuthError>)null);
  }
  
  protected String validateAndHandleErrors(boolean paramBoolean, Predicate<OAuthError> paramPredicate) {
    return validateAndHandleErrors(paramBoolean, paramPredicate, (Set<String>)null);
  }
  
  protected String validateAndHandleErrors(boolean paramBoolean, Predicate<OAuthError> paramPredicate, Set<String> paramSet) {
    String str = super.validateAndHandleErrors(paramBoolean, paramPredicate, paramSet);
    if (str != null)
      deleteLoginIntent(); 
    return str;
  }
}
