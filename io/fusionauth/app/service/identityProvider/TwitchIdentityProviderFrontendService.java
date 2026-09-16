package io.fusionauth.app.service.identityProvider;

import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.TwitchIdentityProvider;
import org.primeframework.mvc.util.QueryStringBuilder;

public class TwitchIdentityProviderFrontendService implements IdentityProviderFrontendService {
  public IdentityProviderFrontendService.PostDataToExternalIDP buildPostData(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    throw new UnsupportedOperationException("The Twitch Identity Provider does not support POST bindings.");
  }
  
  public String buildRedirectURI(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    TwitchIdentityProvider twitchIdentityProvider = (TwitchIdentityProvider)paramFrontendRequestContext.identityProvider;
    return QueryStringBuilder.builder("https://id.twitch.tv/oauth2/authorize")

      
      .with("claims", "{\"userinfo\":{\"email\":null,\"preferred_username\":null}}")
      .with("client_id", twitchIdentityProvider.lookupClientId(paramFrontendRequestContext.client_id))
      .with("login_hint", paramFrontendRequestContext.loginId)
      .with("redirect_uri", paramFrontendRequestContext.fusionAuthURI + "/oauth2/callback")
      .with("response_type", "code")
      .with("scope", twitchIdentityProvider.lookupScope(paramFrontendRequestContext.client_id))
      .with("state", paramFrontendRequestContext.state)
      .build();
  }
}
