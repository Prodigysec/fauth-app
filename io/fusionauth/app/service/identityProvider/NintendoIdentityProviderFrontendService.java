package io.fusionauth.app.service.identityProvider;

import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.NintendoIdentityProvider;
import org.primeframework.mvc.util.QueryStringBuilder;

public class NintendoIdentityProviderFrontendService implements IdentityProviderFrontendService {
  public IdentityProviderFrontendService.PostDataToExternalIDP buildPostData(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    throw new UnsupportedOperationException("The Nintendo Identity Provider does not support POST bindings.");
  }
  
  public String buildRedirectURI(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    NintendoIdentityProvider nintendoIdentityProvider = (NintendoIdentityProvider)paramFrontendRequestContext.identityProvider;
    return QueryStringBuilder.builder("https://accounts.nintendo.com/connect/1.0.0/authorize")
      .with("client_id", nintendoIdentityProvider.lookupClientId(paramFrontendRequestContext.client_id))
      .with("redirect_uri", paramFrontendRequestContext.fusionAuthURI + "/oauth2/callback")
      .with("response_type", "code")
      .with("scope", nintendoIdentityProvider.lookupScope(paramFrontendRequestContext.client_id))
      .with("state", paramFrontendRequestContext.state)
      .build();
  }
}
