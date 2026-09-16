package io.fusionauth.domain.oauth2;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.jwt.DeviceInfo;
import io.fusionauth.domain.provider.PendingIdPLink;
import java.util.UUID;

public class DeviceUserCodeResponse implements Buildable<DeviceUserCodeResponse> {
  public String client_id;
  
  public DeviceInfo deviceInfo;
  
  @JsonProperty("expires_in")
  public Integer expiresIn;
  
  public PendingIdPLink pendingIdPLink;
  
  public String scope;
  
  public UUID tenantId;
  
  public String user_code;
}
