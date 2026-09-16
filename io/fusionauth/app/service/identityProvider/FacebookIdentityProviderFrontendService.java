package io.fusionauth.app.service.identityProvider;

import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.FacebookIdentityProvider;
import org.primeframework.mvc.util.QueryStringBuilder;

public class FacebookIdentityProviderFrontendService implements IdentityProviderFrontendService {
  public IdentityProviderFrontendService.PostDataToExternalIDP buildPostData(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    throw new UnsupportedOperationException("The Facebook Identity Provider does not support POST bindings.");
  }
  
  public String buildRedirectURI(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    FacebookIdentityProvider facebookIdentityProvider = (FacebookIdentityProvider)paramFrontendRequestContext.identityProvider;
    return QueryStringBuilder.builder("https://www.facebook.com/v3.1/dialog/oauth")
      .with("client_id", facebookIdentityProvider.lookupAppId(paramFrontendRequestContext.client_id))
      .with("login_hint", paramFrontendRequestContext.loginId)
      .with("redirect_uri", paramFrontendRequestContext.fusionAuthURI + "/oauth2/callback")
      .with("response_type", "code")
      .with("scope", facebookIdentityProvider.lookupPermissions(paramFrontendRequestContext.client_id))
      .with("state", paramFrontendRequestContext.state)
      .build();
  }
}
