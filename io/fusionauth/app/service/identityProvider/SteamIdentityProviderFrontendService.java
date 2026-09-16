package io.fusionauth.app.service.identityProvider;

import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.SteamIdentityProvider;
import org.primeframework.mvc.util.QueryStringBuilder;

public class SteamIdentityProviderFrontendService implements IdentityProviderFrontendService {
  public IdentityProviderFrontendService.PostDataToExternalIDP buildPostData(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    throw new UnsupportedOperationException("The Steam Identity Provider does not support POST bindings.");
  }
  
  public String buildRedirectURI(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    SteamIdentityProvider steamIdentityProvider = (SteamIdentityProvider)paramFrontendRequestContext.identityProvider;
    return QueryStringBuilder.builder("https://steamcommunity.com/oauth/login")
      .with("client_id", steamIdentityProvider.lookupClientId(paramFrontendRequestContext.client_id))
      .with("login_hint", paramFrontendRequestContext.loginId)
      .with("redirect_uri", paramFrontendRequestContext.fusionAuthURI + "/oauth2/callback/implicit")
      .with("response_type", "token")
      .with("state", paramFrontendRequestContext.state)
      .build();
  }
}
