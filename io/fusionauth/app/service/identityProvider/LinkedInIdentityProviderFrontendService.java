package io.fusionauth.app.service.identityProvider;

import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.LinkedInIdentityProvider;
import org.primeframework.mvc.util.QueryStringBuilder;

public class LinkedInIdentityProviderFrontendService implements IdentityProviderFrontendService {
  public IdentityProviderFrontendService.PostDataToExternalIDP buildPostData(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    throw new UnsupportedOperationException("The LinkedIn Identity Provider does not support POST bindings.");
  }
  
  public String buildRedirectURI(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    LinkedInIdentityProvider linkedInIdentityProvider = (LinkedInIdentityProvider)paramFrontendRequestContext.identityProvider;
    return QueryStringBuilder.builder("https://www.linkedin.com/oauth/v2/authorization")
      .with("client_id", linkedInIdentityProvider.lookupClientId(paramFrontendRequestContext.client_id))
      .with("login_hint", paramFrontendRequestContext.loginId)
      .with("redirect_uri", paramFrontendRequestContext.fusionAuthURI + "/oauth2/callback")
      .with("response_type", "code")
      .with("scope", linkedInIdentityProvider.lookupScope(paramFrontendRequestContext.client_id))
      .with("state", paramFrontendRequestContext.state)
      .build();
  }
}
