package io.fusionauth.domain.oauth2;

public class OAuthCodeAccessTokenRequest {
  public String client_id;
  
  public String client_secret;
  
  public String code;
  
  public String grant_type;
  
  public String redirect_uri;
  
  public String tenantId;
}
