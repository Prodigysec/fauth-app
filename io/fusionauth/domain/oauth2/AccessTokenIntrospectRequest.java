package io.fusionauth.domain.oauth2;

public class AccessTokenIntrospectRequest {
  public String client_id;
  
  public String tenantId;
  
  public String token;
  
  public String token_type_hint;
}
