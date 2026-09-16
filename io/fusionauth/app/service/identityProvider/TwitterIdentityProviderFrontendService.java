package io.fusionauth.app.service.identityProvider;

import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.TwitterIdentityProvider;
import org.primeframework.mvc.util.QueryStringBuilder;

public class TwitterIdentityProviderFrontendService implements IdentityProviderFrontendService {
  public IdentityProviderFrontendService.PostDataToExternalIDP buildPostData(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    throw new UnsupportedOperationException("The Twitter Identity Provider does not support POST bindings.");
  }
  
  public String buildRedirectURI(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    TwitterIdentityProvider twitterIdentityProvider = (TwitterIdentityProvider)paramFrontendRequestContext.identityProvider;
    return QueryStringBuilder.builder("/oauth1/request-token")
      .with("state.client_id", paramFrontendRequestContext.client_id)
      .with("state.identityProviderId", twitterIdentityProvider.id)
      .with("state.state", paramFrontendRequestContext.state)
      .build();
  }
}
