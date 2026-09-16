package io.fusionauth.app.action.oauth2;

import com.google.inject.Inject;
import io.fusionauth.api.service.oauth2.OAuthService;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.freemarker.FTLVariable;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.oauth2.LogoutBehavior;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.primeframework.mvc.ErrorException;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;
import org.primeframework.mvc.util.QueryStringBuilder;
import org.primeframework.mvc.validation.ValidationMethod;

@Action
@Redirect(code = "default-redirect", uri = "/")
public class LogoutAction extends BaseOAuthAction {
  @FTLVariable
  public List<String> allLogoutURLs = new ArrayList<>();
  
  public String id_token_hint;
  
  public String post_logout_redirect_uri;
  
  @FTLVariable
  public String redirectURL;
  
  @FTLVariable
  public List<String> registeredLogoutURLs = new ArrayList<>();
  
  private OAuthService.OAuthValidationResult result;
  
  @Inject
  protected LogoutAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, OAuthService paramOAuthService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramOAuthService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    this.ssoService.logout(this.ssoSession, this.ssoCookie, this.codeTenant, this.frontEndSupport.buildEventInfo(this.metaData));
    if (this.result.redirectURL == null)
      return "default-redirect"; 
    this
      
      .redirectURL = QueryStringBuilder.builder(this.result.redirectURL).with("state", this.state).build();
    if (this.codeUser != null)
      this.registeredLogoutURLs.addAll(this.result.logoutURLs.keySet()
          .stream()
          .filter(paramUUID -> (this.codeUser.getRegistrationForApplication(paramUUID) != null))
          .map(paramUUID -> (String)this.result.logoutURLs.get(paramUUID))
          .toList()); 
    if (this.result.application.oauthConfiguration.logoutBehavior == LogoutBehavior.RedirectOnly) {
      this.authorizedRedirectURI = this.redirectURL;
      return "authorized-redirect";
    } 
    return "input";
  }
  
  public String post() {
    return get();
  }
  
  @PostParameterMethod
  public void setup() {
    this.redirectURL = null;
    resolveUserAndZoneId();
  }
  
  @ValidationMethod(httpMethods = {"GET", "POST"})
  public void validateGet() {
    this.result = this.oauthService.validateLogoutRequest(this.tenantId, this.client_id, this.id_token_hint, this.post_logout_redirect_uri, this.ssoSession);
    if (this.result.error != null) {
      this.oauthJSONError = this.frontEndSupport.writeToPrettyString(this.result.error);
      throw new ErrorException("render-error", false);
    } 
    this.allLogoutURLs.addAll(this.result.logoutURLs.values());
  }
}
