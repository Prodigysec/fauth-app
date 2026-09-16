package io.fusionauth.domain.oauth2;

import java.util.UUID;

public class ValidateDeviceRequest {
  public String client_id;
  
  public UUID tenantId;
  
  public String user_code;
}
