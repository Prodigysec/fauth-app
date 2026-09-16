package io.fusionauth.app.action.oauth2;

import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

public abstract class BaseOAuthAuthenticationAction extends BaseOAuthAction {
  protected BaseOAuthAuthenticationAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  @PostParameterMethod
  public void clearLoginIntent() {
    this.loginIntentCookie.value = null;
    resolveUserAndZoneId();
  }
}
