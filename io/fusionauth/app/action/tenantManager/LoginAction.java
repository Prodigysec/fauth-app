package io.fusionauth.app.action.tenantManager;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.api.util.PKCETools;
import io.fusionauth.app.action.BaseOAuthCallbackAction;
import io.fusionauth.app.guice.TenantManagerApplicationId;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Forward;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.action.result.annotation.Redirect.List;
import org.primeframework.mvc.security.UserLoginSecurityContext;
import org.primeframework.mvc.security.oauth.Tokens;
import org.primeframework.mvc.util.QueryStringBuilder;

@Action
@List({@Redirect(code = "authorize", uri = "${authorizedRedirectURI}")})
@Forward(code = "meta-refresh", page = "/tenant-manager/meta-refresh.ftl")
@ThemedForward(code = "invalid-csrf-token", page = "/oauth2/error.ftl")
public class LoginAction extends BaseOAuthCallbackAction {
  private final UserLoginSecurityContext userLoginSecurityContext;
  
  public String authorizedRedirectURI;
  
  @Inject
  protected LoginAction(FrontEndSupport paramFrontEndSupport, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, @Named("TenantManagerSecurityContext") UserLoginSecurityContext paramUserLoginSecurityContext, @TenantManagerApplicationId UUID paramUUID, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
    this.userLoginSecurityContext = paramUserLoginSecurityContext;
    this.client_id = paramUUID.toString();
  }
  
  public String get() {
    if (this.code == null) {
      this.frontEndSupport.messageStore.clear();
      this.state = this.frontEndSupport.storeCSRFToken();
      this.codeVerifier.setValue(PKCETools.generateCodeVerifier());
      this









        
        .authorizedRedirectURI = QueryStringBuilder.builder("/oauth2/authorize").with("client_id", this.client_id).with("code_challenge", PKCETools.generateCodeChallenge(this.codeVerifier.getValue())).with("code_challenge_method", "S256").with("redirect_uri", "/tenant-manager/login?client_id=" + this.client_id + "&tenantId=" + String.valueOf(this.tenantId)).with("response_type", "code").with("scope", "openid offline_access").with("state", this.state).with("timezone", this.timezone).with("tenantId", this.tenantId).build();
      return "authorize";
    } 
    this.frontEndSupport.validateCSRFToken(this.state);
    BaseOAuthCallbackAction.OAuthResult oAuthResult = exchangeCodeForToken(this.codeApplication, "/tenant-manager/login?client_id=" + this.client_id + "&tenantId=" + String.valueOf(this.tenantId));
    if (oAuthResult == null)
      return "render-error"; 
    this.userLoginSecurityContext.login(new Tokens(oAuthResult.accessToken.token, oAuthResult.accessToken.refreshToken));
    return "meta-refresh";
  }
}
