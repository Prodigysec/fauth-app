package io.fusionauth.domain.api.passwordless;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import java.util.Map;
import java.util.UUID;

public class PasswordlessSendRequest implements Buildable<PasswordlessSendRequest> {
  public UUID applicationId;
  
  public String code;
  
  public String loginId;
  
  public Map<String, Object> state;
  
  @JacksonConstructor
  public PasswordlessSendRequest() {}
  
  public PasswordlessSendRequest(String paramString) {
    this.code = paramString;
  }
  
  @Deprecated
  public PasswordlessSendRequest(UUID paramUUID, String paramString1, String paramString2) {
    this.applicationId = paramUUID;
    this.loginId = paramString1;
    this.code = paramString2;
  }
  
  public PasswordlessSendRequest(UUID paramUUID, String paramString, Map<String, Object> paramMap) {
    this.applicationId = paramUUID;
    this.loginId = paramString;
    this.state = paramMap;
  }
}
