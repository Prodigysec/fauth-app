package io.fusionauth.domain.oauth2;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.jwt.DeviceInfo;
import java.util.UUID;

public class DeviceApprovalResponse implements Buildable<DeviceApprovalResponse> {
  public String deviceGrantStatus;
  
  public DeviceInfo deviceInfo;
  
  public IdentityProviderLink identityProviderLink;
  
  public UUID tenantId;
  
  public UUID userId;
}
