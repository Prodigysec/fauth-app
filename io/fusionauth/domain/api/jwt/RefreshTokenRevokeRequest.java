package io.fusionauth.domain.api.jwt;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.api.BaseEventRequest;
import java.util.UUID;

public class RefreshTokenRevokeRequest extends BaseEventRequest implements Buildable<RefreshTokenRevokeRequest> {
  public UUID applicationId;
  
  public String token;
  
  public UUID userId;
  
  @JacksonConstructor
  public RefreshTokenRevokeRequest() {}
  
  public RefreshTokenRevokeRequest(EventInfo paramEventInfo, UUID paramUUID1, String paramString, UUID paramUUID2) {
    super(paramEventInfo);
    this.applicationId = paramUUID1;
    this.token = paramString;
    this.userId = paramUUID2;
  }
}
