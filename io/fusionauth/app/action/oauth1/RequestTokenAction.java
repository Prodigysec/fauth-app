package io.fusionauth.app.action.oauth1;

import com.google.inject.Inject;
import io.fusionauth.api.service.authentication.IdentityProviderAuthenticationService;
import io.fusionauth.api.service.authentication.OAuth1IdentityProviderAuthenticationService;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.security.ThreatDetectionService;
import io.fusionauth.app.action.BaseThemedAction;
import io.fusionauth.app.primeframework.ThemedForward;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.app.service.FrontEndThemeResolver;
import io.fusionauth.app.service.security.LoginIntentService;
import io.fusionauth.app.service.security.SSOService;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.util.Map;
import java.util.UUID;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.action.result.annotation.Redirect;
import org.primeframework.mvc.scope.annotation.BrowserActionSession;

@Action
@Redirect(code = "redirect", uri = "${redirect}")
@ThemedForward(code = "external-authentication-exception", page = "/oauth2/error.ftl", status = 401)
public class RequestTokenAction extends BaseThemedAction {
  private final Map<IdentityProviderType, IdentityProviderAuthenticationService> identityProviderAuthenticationServices;
  
  private final IdentityProviderCache identityProviderCache;
  
  public String redirect;
  
  @BrowserActionSession
  public OAuth1 state;
  
  @Inject
  public RequestTokenAction(FrontEndSupport paramFrontEndSupport, IdentityProviderCache paramIdentityProviderCache, Map<IdentityProviderType, IdentityProviderAuthenticationService> paramMap, FrontEndThemeResolver paramFrontEndThemeResolver, LoginIntentService paramLoginIntentService, SSOService paramSSOService, ThreatDetectionService paramThreatDetectionService) {
    super(paramFrontEndSupport, paramFrontEndThemeResolver, paramLoginIntentService, paramSSOService, paramThreatDetectionService);
    this.identityProviderCache = paramIdentityProviderCache;
    this.identityProviderAuthenticationServices = paramMap;
  }
  
  public String get() {
    OAuth1IdentityProviderAuthenticationService oAuth1IdentityProviderAuthenticationService;
    BaseIdentityProvider baseIdentityProvider = (BaseIdentityProvider)this.identityProviderCache.get(this.state.identityProviderId);
    if (baseIdentityProvider == null)
      return "render-error"; 
    IdentityProviderAuthenticationService identityProviderAuthenticationService = this.identityProviderAuthenticationServices.get(baseIdentityProvider.getType());
    if (identityProviderAuthenticationService instanceof OAuth1IdentityProviderAuthenticationService) {
      oAuth1IdentityProviderAuthenticationService = (OAuth1IdentityProviderAuthenticationService)identityProviderAuthenticationService;
    } else {
      return "render-error";
    } 
    OAuth1IdentityProviderAuthenticationService.RequestToken requestToken = oAuth1IdentityProviderAuthenticationService.requestRequestToken(this.state.client_id, this.frontEndSupport.getFusionAuthBaseURL() + "/oauth2/callback", baseIdentityProvider.id);
    this.state.oauth_token = requestToken.oauth_token;
    this.state.oauth_token_secret = requestToken.oauth_token_secret;
    this.redirect = oAuth1IdentityProviderAuthenticationService.authenticateURI() + "?oauth_token=" + oAuth1IdentityProviderAuthenticationService.authenticateURI();
    return "redirect";
  }
  
  public static class OAuth1 {
    public String client_id;
    
    public UUID identityProviderId;
    
    public String oauth_token;
    
    public String oauth_token_secret;
    
    public String state;
  }
}
