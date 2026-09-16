package io.fusionauth.domain.api.passwordless;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.PasswordlessStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PasswordlessStartRequest implements Buildable<PasswordlessStartRequest> {
  public UUID applicationId;
  
  public String loginId;
  
  public List<String> loginIdTypes = new ArrayList<>();
  
  public PasswordlessStrategy loginStrategy;
  
  public Map<String, Object> state;
  
  @JacksonConstructor
  public PasswordlessStartRequest() {}
  
  public PasswordlessStartRequest(UUID paramUUID, String paramString, List<String> paramList, Map<String, Object> paramMap) {
    this.applicationId = paramUUID;
    this.loginId = paramString;
    this.loginIdTypes = paramList;
    this.state = paramMap;
  }
}
