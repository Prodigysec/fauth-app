package io.fusionauth.domain.oauth2;

import java.util.UUID;

public class RefreshTokenAccessTokenRequest {
  public String client_id;
  
  public String client_secret;
  
  public String grant_type;
  
  public String refresh_token;
  
  public String scope;
  
  public UUID tenantId;
  
  public String user_code;
}
