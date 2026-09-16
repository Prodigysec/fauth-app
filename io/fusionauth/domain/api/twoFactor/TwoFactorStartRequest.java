package io.fusionauth.domain.api.twoFactor;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TwoFactorStartRequest implements Buildable<TwoFactorStartRequest> {
  public UUID applicationId;
  
  public String code;
  
  public String loginId;
  
  public List<String> loginIdTypes = new ArrayList<>();
  
  public Map<String, Object> state;
  
  public String trustChallenge;
  
  public UUID userId;
  
  @JacksonConstructor
  public TwoFactorStartRequest() {}
  
  public TwoFactorStartRequest(UUID paramUUID1, String paramString, UUID paramUUID2) {
    this.applicationId = paramUUID1;
    this.code = paramString;
    this.userId = paramUUID2;
  }
  
  public TwoFactorStartRequest(UUID paramUUID, String paramString1, String paramString2) {
    this.applicationId = paramUUID;
    this.code = paramString1;
    this.loginId = paramString2;
  }
}
