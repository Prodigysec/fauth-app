package io.fusionauth.domain.oauth2;

import java.util.UUID;

public class DeviceAuthorizationRequest {
  public String client_id;
  
  public String client_secret;
  
  public String scope;
  
  public UUID tenantId;
}
