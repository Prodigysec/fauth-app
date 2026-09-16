package io.fusionauth.app.action.account;

import com.google.inject.Inject;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.util.PKCETools;
import io.fusionauth.app.action.BaseOAuthCallbackAction;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.User;
import io.fusionauth.domain.oauth2.OAuthError;
import java.util.Locale;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.security.oauth.Tokens;
import org.primeframework.mvc.util.QueryStringBuilder;

@Action
@Forward(code = "meta-refresh", page = "/account/meta-refresh.ftl")
@List({@Redirect(code = "authorize", uri = "${authorizedRedirectURI}")})
@ThemedForward(code = "invalid-csrf-token", page = "/oauth2/error.ftl")
public class LoginAction extends BaseOAuthCallbackAction {
  public String authorizedRedirectURI;
  
  @Inject
  public LoginAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
  }
  
  public String get() {
    if (this.client_id == null) {
      this.oauthJSONError = this.frontEndSupport.writeToPrettyString(new OAuthError(OAuthError.OAuthErrorType.invalid_request, OAuthError.OAuthErrorReason.missing_client_id, "The request is missing a required parameter: client_id"));
      return "render-error";
    } 
    if (this.code == null) {
      this.frontEndSupport.messageStore.clear();
      this.state = this.frontEndSupport.storeCSRFToken();
      this.codeVerifier.setValue(PKCETools.generateCodeVerifier());
      this








        
        .authorizedRedirectURI = QueryStringBuilder.builder("/oauth2/authorize").with("client_id", this.client_id).with("code_challenge", PKCETools.generateCodeChallenge(this.codeVerifier.getValue())).with("code_challenge_method", "S256").with("redirect_uri", "/account/login?client_id=" + this.client_id + "&tenantId=" + String.valueOf(this.tenantId)).with("response_type", "code").with("scope", "offline_access").with("state", this.state).with("timezone", this.timezone).with("tenantId", this.tenantId).build();
      return "authorize";
    } 
    this.frontEndSupport.validateCSRFToken(this.state);
    BaseOAuthCallbackAction.OAuthResult oAuthResult = exchangeCodeForToken(this.codeApplication, "/account/login?client_id=" + this.client_id + "&tenantId=" + String.valueOf(this.tenantId));
    if (oAuthResult == null)
      return "render-error"; 
    setupAccountRequestAttributes(this.codeApplication, this.frontEndSupport.request);
    this.frontEndSupport.userLoginSecurityContext.login(new Tokens(oAuthResult.accessToken.token, oAuthResult.accessToken.refreshToken));
    User user = oAuthResult.user;
    Locale locale = user.lookupPreferredLanguage(this.codeApplication.id);
    if (locale != null)
      this.frontEndSupport.localeProvider.set(locale); 
    return "meta-refresh";
  }
}
