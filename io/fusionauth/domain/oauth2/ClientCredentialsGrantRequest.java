package io.fusionauth.domain.oauth2;

public class ClientCredentialsGrantRequest {
  public String client_id;
  
  public String client_secret;
  
  public String grant_type;
  
  public String scope;
  
  public String tenantId;
}
