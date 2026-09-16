package io.fusionauth.app.service.identityProvider;

import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.AppleIdentityProvider;
import org.primeframework.mvc.util.QueryStringBuilder;

public class AppleIdentityProviderFrontendService implements IdentityProviderFrontendService {
  public IdentityProviderFrontendService.PostDataToExternalIDP buildPostData(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    throw new UnsupportedOperationException("The Apple Identity Provider does not support POST bindings.");
  }
  
  public String buildRedirectURI(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    AppleIdentityProvider appleIdentityProvider = (AppleIdentityProvider)paramFrontendRequestContext.identityProvider;
    return QueryStringBuilder.builder("https://appleid.apple.com/auth/authorize")
      .with("client_id", appleIdentityProvider.lookupServicesId(paramFrontendRequestContext.client_id))
      .with("login_hint", paramFrontendRequestContext.loginId)
      .with("redirect_uri", paramFrontendRequestContext.fusionAuthURI + "/oauth2/callback")
      .with("response_type", "code id_token")
      .with("response_mode", "form_post")
      .with("scope", appleIdentityProvider.lookupScope(paramFrontendRequestContext.client_id))
      .with("state", paramFrontendRequestContext.state)
      .build();
  }
}
