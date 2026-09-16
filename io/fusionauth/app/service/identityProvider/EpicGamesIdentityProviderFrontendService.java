package io.fusionauth.app.service.identityProvider;

import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.EpicGamesIdentityProvider;
import org.primeframework.mvc.util.QueryStringBuilder;

public class EpicGamesIdentityProviderFrontendService implements IdentityProviderFrontendService {
  public IdentityProviderFrontendService.PostDataToExternalIDP buildPostData(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    throw new UnsupportedOperationException("The Epic Games Identity Provider does not support POST bindings.");
  }
  
  public String buildRedirectURI(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    EpicGamesIdentityProvider epicGamesIdentityProvider = (EpicGamesIdentityProvider)paramFrontendRequestContext.identityProvider;
    return QueryStringBuilder.builder("https://www.epicgames.com/id/authorize")
      .with("client_id", epicGamesIdentityProvider.lookupClientId(paramFrontendRequestContext.client_id))
      .with("login_hint", paramFrontendRequestContext.loginId)
      .with("redirect_uri", paramFrontendRequestContext.fusionAuthURI + "/oauth2/callback")
      .with("response_type", "code")
      .with("scope", epicGamesIdentityProvider.lookupScope(paramFrontendRequestContext.client_id))
      .with("state", paramFrontendRequestContext.state)
      .build();
  }
}
