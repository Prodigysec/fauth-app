package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
public class StartIdpLinkAction extends BaseOAuthAuthenticationAction {
  @FTLVariable
  public boolean registrationEnabled;
  
  @Inject
  public StartIdpLinkAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    this.registrationEnabled = this.codeApplication.registrationConfiguration.enabled;
    return "input";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateOAuth() {
    String str = validateAndHandleErrors(false);
    if (str != null)
      throw new ErrorException(str, false); 
    if (this.pendingIdPLinkId == null && this.devicePendingIdPLink == null) {
      addGeneralError("[MissingPendingIdPLinkId]", new Object[0]);
      buildRedirectToAuthorizeURI();
      throw new ErrorException("redirect-to-authorize");
    } 
  }
}
