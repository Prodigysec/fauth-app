package io.fusionauth.app.service.identityProvider;

import com.google.inject.Inject;
import io.fusionauth.api.util.PKCETools;
import io.fusionauth.app.Cookies;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import io.fusionauth.http.server.HTTPRequest;
import io.fusionauth.http.server.HTTPResponse;
import org.primeframework.mvc.util.QueryStringBuilder;

public class OpenIDConnectIdentityProviderFrontendService implements IdentityProviderFrontendService {
  private final HTTPRequest request;
  
  private final HTTPResponse response;
  
  @Inject
  public OpenIDConnectIdentityProviderFrontendService(HTTPRequest paramHTTPRequest, HTTPResponse paramHTTPResponse) {
    this.request = paramHTTPRequest;
    this.response = paramHTTPResponse;
  }
  
  public IdentityProviderFrontendService.PostDataToExternalIDP buildPostData(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    OpenIdConnectIdentityProvider openIdConnectIdentityProvider = (OpenIdConnectIdentityProvider)paramFrontendRequestContext.identityProvider;
    IdentityProviderFrontendService.PostDataToExternalIDP postDataToExternalIDP = new IdentityProviderFrontendService.PostDataToExternalIDP();
    postDataToExternalIDP.uri = openIdConnectIdentityProvider.oauth2.authorization_endpoint;
    postDataToExternalIDP.formData.put("client_id", openIdConnectIdentityProvider.lookupClientId(paramFrontendRequestContext.client_id));
    String str1 = PKCETools.generateCodeVerifier();
    String str2 = PKCETools.generateCodeChallenge(str1);
    Cookies.addHttpOnlySession(this.request, this.response, "fusionauth.pkce-verifier", str1, null);
    postDataToExternalIDP.formData.put("code_challenge", str2);
    postDataToExternalIDP.formData.put("code_challenge_method", "S256");
    postDataToExternalIDP.formData.put("login_hint", paramFrontendRequestContext.loginId);
    if (paramFrontendRequestContext.prompt != null)
      postDataToExternalIDP.formData.put("prompt", paramFrontendRequestContext.prompt); 
    postDataToExternalIDP.formData.put("redirect_uri", paramFrontendRequestContext.fusionAuthURI + "/oauth2/callback");
    postDataToExternalIDP.formData.put("response_type", "code");
    postDataToExternalIDP.formData.put("scope", openIdConnectIdentityProvider.lookupScope(paramFrontendRequestContext.client_id));
    postDataToExternalIDP.formData.put("state", paramFrontendRequestContext.state);
    return postDataToExternalIDP;
  }
  
  public String buildRedirectURI(Tenant paramTenant, IdentityProviderFrontendService.FrontendRequestContext paramFrontendRequestContext) {
    String str1 = PKCETools.generateCodeVerifier();
    String str2 = PKCETools.generateCodeChallenge(str1);
    Cookies.addHttpOnlySession(this.request, this.response, "fusionauth.pkce-verifier", str1, null);
    OpenIdConnectIdentityProvider openIdConnectIdentityProvider = (OpenIdConnectIdentityProvider)paramFrontendRequestContext.identityProvider;
    return QueryStringBuilder.builder(openIdConnectIdentityProvider.oauth2.authorization_endpoint.toString())
      .with("code_challenge", str2)
      .with("code_challenge_method", "S256")
      .with("client_id", openIdConnectIdentityProvider.lookupClientId(paramFrontendRequestContext.client_id))
      .with("login_hint", paramFrontendRequestContext.loginId)
      .with("prompt", paramFrontendRequestContext.prompt)
      .with("redirect_uri", paramFrontendRequestContext.fusionAuthURI + "/oauth2/callback")
      .with("response_type", "code")
      .with("scope", openIdConnectIdentityProvider.lookupScope(paramFrontendRequestContext.client_id))
      .with("state", paramFrontendRequestContext.state)
      .build();
  }
}
