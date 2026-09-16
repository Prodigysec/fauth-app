package io.fusionauth.domain.oauth2;

import java.util.UUID;

public class RetrieveUserCodeRequest {
  public String client_id;
  
  public String client_secret;
  
  public UUID tenantId;
  
  public String user_code;
}
