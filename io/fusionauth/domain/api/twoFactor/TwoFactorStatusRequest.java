package io.fusionauth.domain.api.twoFactor;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.MultiFactorAction;
import io.fusionauth.domain.api.BaseEventRequest;
import java.util.UUID;

public class TwoFactorStatusRequest extends BaseEventRequest {
  public final UUID userId;
  
  public String accessToken;
  
  public MultiFactorAction action = MultiFactorAction.login;
  
  public UUID applicationId;
  
  public String twoFactorTrustId;
  
  public TwoFactorStatusRequest(UUID paramUUID) {
    this.userId = paramUUID;
  }
  
  @JacksonConstructor
  private TwoFactorStatusRequest() {
    this.userId = null;
  }
}
