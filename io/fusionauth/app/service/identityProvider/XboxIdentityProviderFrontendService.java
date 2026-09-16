package io.fusionauth.app.service.identityProvider;

import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.XboxIdentityProvider;
import org.primeframework.mvc.util.QueryStringBuilder;

public class XboxIdentityProviderFrontendService implements IdentityProviderFrontendService {
  public IdentityProviderFrontendService.PostDataToExternalIDP buildPostData(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    throw new UnsupportedOperationException("The Xbox Identity Provider does not support POST bindings.");
  }
  
  public String buildRedirectURI(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    XboxIdentityProvider xboxIdentityProvider = (XboxIdentityProvider)paramFrontendRequestContext.identityProvider;
    return QueryStringBuilder.builder("https://login.live.com/oauth20_authorize.srf")
      .with("client_id", xboxIdentityProvider.lookupClientId(paramFrontendRequestContext.client_id))
      .with("login_hint", paramFrontendRequestContext.loginId)
      .with("redirect_uri", paramFrontendRequestContext.fusionAuthURI + "/oauth2/callback")
      .with("response_type", "code")
      .with("scope", xboxIdentityProvider.lookupScope(paramFrontendRequestContext.client_id))
      .with("approval_prompt", "auto")
      .with("state", paramFrontendRequestContext.state)
      .build();
  }
}
