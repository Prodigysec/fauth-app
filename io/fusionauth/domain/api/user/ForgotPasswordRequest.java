package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.api.BaseEventRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ForgotPasswordRequest extends BaseEventRequest implements Buildable<ForgotPasswordRequest> {
  public UUID applicationId;
  
  public String changePasswordId;
  
  public String loginId;
  
  public List<String> loginIdTypes;
  
  @Deprecated
  public boolean sendForgotPasswordEmail = true;
  
  public Boolean sendForgotPasswordMessage;
  
  public Map<String, Object> state;
  
  @JacksonConstructor
  public ForgotPasswordRequest() {}
  
  public ForgotPasswordRequest(String paramString) {
    this.loginId = paramString;
  }
  
  public ForgotPasswordRequest(String paramString, Map<String, Object> paramMap) {
    this.loginId = paramString;
    this.state = paramMap;
  }
  
  public ForgotPasswordRequest(UUID paramUUID, String paramString) {
    this.applicationId = paramUUID;
    this.loginId = paramString;
  }
  
  public ForgotPasswordRequest(String paramString, boolean paramBoolean) {
    this.loginId = paramString;
    this.sendForgotPasswordMessage = Boolean.valueOf(paramBoolean);
  }
  
  public ForgotPasswordRequest(EventInfo paramEventInfo, UUID paramUUID, String paramString) {
    super(paramEventInfo);
    this.applicationId = paramUUID;
    this.loginId = paramString;
  }
  
  public ForgotPasswordRequest(EventInfo paramEventInfo, String paramString, Map<String, Object> paramMap) {
    super(paramEventInfo);
    this.loginId = paramString;
    this.state = paramMap;
  }
  
  public String getEmail() {
    return this.loginId;
  }
  
  public void setEmail(String paramString) {
    this.loginId = paramString;
  }
  
  public String getUsername() {
    return this.loginId;
  }
  
  public void setUsername(String paramString) {
    this.loginId = paramString;
  }
}
