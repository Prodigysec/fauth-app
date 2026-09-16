package io.fusionauth.app.action.password;

import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.oauth2.BaseOAuthAction;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@Action
@Redirect(code = "return", uri = "${redirectURI}")
public class SentAction extends BaseOAuthAction {
  @Deprecated(since = "1.59.0")
  @FTLVariable
  public String email;
  
  public String redirectURI;
  
  @Inject
  public SentAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    if (this.loginId == null) {
      this
        .redirectURI = baseQueryBuilder("/password/forgot").build();
      return "return";
    } 
    return "input";
  }
  
  @PostParameterMethod
  public void postParameter() {
    this.email = this.loginId;
  }
}
