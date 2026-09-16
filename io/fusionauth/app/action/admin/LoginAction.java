package io.fusionauth.app.action.admin;

import com.google.inject.Inject;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.util.PKCETools;
import io.fusionauth.app.action.BaseOAuthCallbackAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.oauth2.UserState;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Forward.List;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.security.oauth.Tokens;

@Action
@List({@Redirect(code = "authorize", uri = "/oauth2/authorize?client_id=${fusionAuthId}&response_type=code&redirect_uri=${encodedRedirectURI}&scope=offline_access&code_challenge=${codeChallenge}&code_challenge_method=S256&state=${state}", encodeVariables = false), @Redirect(code = "setup-wizard", uri = "/admin/setup-wizard")})
@List({@Forward(code = "invalid-csrf-token", cacheControl = "no-store"), @Forward(code = "meta-refresh", page = "/admin/meta-refresh.ftl")})
public class LoginAction extends BaseOAuthCallbackAction {
  public final String encodedRedirectURI;
  
  private final String redirectURI;
  
  public String codeChallenge;
  
  @Inject
  public LoginAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
    this.client_id = Application.FUSIONAUTH_APP_ID.toString();
    this.redirectURI = "/admin/login";
    this.encodedRedirectURI = URLEncoder.encode(this.redirectURI, StandardCharsets.UTF_8);
  }
  
  public String get() {
    if (!(this.frontEndSupport.getInstance()).setupComplete)
      if (this.internalReset == null || this.internalReset.isDone())
        return "setup-wizard";  
    if (this.code == null) {
      this.frontEndSupport.messageStore.clear();
      this.state = this.frontEndSupport.storeCSRFToken();
      this.codeVerifier.setValue(PKCETools.generateCodeVerifier());
      this.codeChallenge = PKCETools.generateCodeChallenge(this.codeVerifier.getValue());
      return "authorize";
    } 
    this.frontEndSupport.validateCSRFToken(this.state);
    if (this.userState == UserState.AuthenticatedNotRegistered) {
      addGeneralError("[UserNotRegisteredException]", new Object[0]);
      return "input";
    } 
    Application application = loadApplication(Application.FUSIONAUTH_APP_ID);
    BaseOAuthCallbackAction.OAuthResult oAuthResult = exchangeCodeForToken(application, this.redirectURI);
    if (oAuthResult == null)
      return "input"; 
    User user = oAuthResult.user;
    UserRegistration userRegistration = user.getRegistrationForApplication(Application.FUSIONAUTH_APP_ID);
    if (userRegistration == null) {
      addGeneralError("[UserNotRegisteredException]", new Object[0]);
      return "input";
    } 
    if (userRegistration.roles.isEmpty()) {
      addGeneralError("[UserRegisteredWithoutRoles]", new Object[0]);
      return "input";
    } 
    if (this.passwordChanged)
      this.frontEndSupport.addGeneralInfo("[PasswordChangeSuccessful]", new Object[0]); 
    this.frontEndSupport.userLoginSecurityContext.login(new Tokens(oAuthResult.accessToken.token, oAuthResult.accessToken.refreshToken));
    Locale locale = user.lookupPreferredLanguage(application.id);
    if (locale != null)
      this.frontEndSupport.localeProvider.set(locale); 
    return "meta-refresh";
  }
  
  public String post() {
    return get();
  }
}
