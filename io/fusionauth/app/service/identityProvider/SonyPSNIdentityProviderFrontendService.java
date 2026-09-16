package io.fusionauth.app.service.identityProvider;

import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.SonyPSNIdentityProvider;
import org.primeframework.mvc.util.QueryStringBuilder;

public class SonyPSNIdentityProviderFrontendService implements IdentityProviderFrontendService {
  public IdentityProviderFrontendService.PostDataToExternalIDP buildPostData(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    throw new UnsupportedOperationException("The Sony PlayStation Network Identity Provider does not support POST bindings.");
  }
  
  public String buildRedirectURI(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    SonyPSNIdentityProvider sonyPSNIdentityProvider = (SonyPSNIdentityProvider)paramFrontendRequestContext.identityProvider;
    return QueryStringBuilder.builder("https://ca.account.sony.com/api/v1/oauth/authorize")
      .with("client_id", sonyPSNIdentityProvider.lookupClientId(paramFrontendRequestContext.client_id))
      .with("login_hint", paramFrontendRequestContext.loginId)


      
      .with("redirect_uri", paramFrontendRequestContext.fusionAuthURI + "/oauth2/callback")
      .with("response_type", "code")
      .with("scope", sonyPSNIdentityProvider.lookupScope(paramFrontendRequestContext.client_id))
      .with("service_entity", "urn:service-entity:psn")
      .with("state", paramFrontendRequestContext.state)
      .build();
  }
}
