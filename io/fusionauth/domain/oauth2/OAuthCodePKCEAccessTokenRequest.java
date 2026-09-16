package io.fusionauth.domain.oauth2;

import java.util.UUID;

public class OAuthCodePKCEAccessTokenRequest {
  public String client_id;
  
  public String client_secret;
  
  public String code;
  
  public String code_verifier;
  
  public String grant_type;
  
  public String redirect_uri;
  
  public UUID tenantId;
}
