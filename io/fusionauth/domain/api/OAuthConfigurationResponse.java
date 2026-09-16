package io.fusionauth.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.oauth2.OAuth2Configuration;
import java.net.URI;

public class OAuthConfigurationResponse {
  public int httpSessionMaxInactiveInterval;
  
  public URI logoutURL;
  
  @JsonIgnoreProperties({"debug"})
  public OAuth2Configuration oauthConfiguration;
  
  public OAuthConfigurationResponse(int paramInt, URI paramURI, OAuth2Configuration paramOAuth2Configuration) {
    this.httpSessionMaxInactiveInterval = paramInt;
    this.logoutURL = paramURI;
    this.oauthConfiguration = paramOAuth2Configuration;
  }
  
  @JacksonConstructor
  public OAuthConfigurationResponse() {}
}
