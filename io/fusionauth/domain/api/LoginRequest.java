package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LoginRequest extends BaseLoginRequest implements Buildable<LoginRequest> {
  public String loginId;
  
  public List<String> loginIdTypes = new ArrayList<>();
  
  public String oneTimePassword;
  
  public String password;
  
  public String twoFactorTrustId;
  
  @JacksonConstructor
  public LoginRequest() {}
  
  public LoginRequest(UUID paramUUID, String paramString1, String paramString2) {
    this.applicationId = paramUUID;
    this.loginId = paramString1;
    this.password = paramString2;
  }
  
  @Deprecated
  public LoginRequest(UUID paramUUID, String paramString1, String paramString2, String paramString3) {
    this.applicationId = paramUUID;
    this.loginId = paramString1;
    this.password = paramString2;
    if (paramString3 != null) {
      this.eventInfo = (this.eventInfo != null) ? this.eventInfo : new EventInfo();
      this.eventInfo.ipAddress = paramString3;
    } 
  }
  
  public LoginRequest(EventInfo paramEventInfo, UUID paramUUID, String paramString1, String paramString2) {
    super(paramEventInfo);
    this.applicationId = paramUUID;
    this.loginId = paramString1;
    this.password = paramString2;
  }
  
  public LoginRequest(EventInfo paramEventInfo, UUID paramUUID, String paramString1, List<String> paramList, String paramString2) {
    super(paramEventInfo);
    this.applicationId = paramUUID;
    this.loginId = paramString1;
    this.loginIdTypes = paramList;
    this.password = paramString2;
  }
}
