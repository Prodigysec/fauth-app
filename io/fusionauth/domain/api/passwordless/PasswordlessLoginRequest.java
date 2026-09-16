package io.fusionauth.domain.api.passwordless;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.api.BaseLoginRequest;

public class PasswordlessLoginRequest extends BaseLoginRequest implements Buildable<PasswordlessLoginRequest> {
  public String code;
  
  public String oneTimeCode;
  
  public String twoFactorTrustId;
  
  @JacksonConstructor
  public PasswordlessLoginRequest() {}
  
  public PasswordlessLoginRequest(String paramString) {
    this.code = paramString;
  }
  
  public PasswordlessLoginRequest(String paramString1, String paramString2) {
    this.code = paramString1;
    if (paramString2 != null) {
      this.eventInfo = (this.eventInfo != null) ? this.eventInfo : new EventInfo();
      this.eventInfo.ipAddress = paramString2;
    } 
  }
  
  public PasswordlessLoginRequest(EventInfo paramEventInfo, String paramString) {
    super(paramEventInfo);
    this.code = paramString;
  }
}
