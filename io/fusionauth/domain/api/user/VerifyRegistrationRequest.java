package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.api.BaseEventRequest;

public class VerifyRegistrationRequest extends BaseEventRequest {
  public String oneTimeCode;
  
  public String verificationId;
  
  @JacksonConstructor
  public VerifyRegistrationRequest() {}
  
  public VerifyRegistrationRequest(String paramString1, String paramString2) {
    this.oneTimeCode = paramString1;
    this.verificationId = paramString2;
  }
  
  public VerifyRegistrationRequest(String paramString) {
    this.verificationId = paramString;
  }
  
  public VerifyRegistrationRequest(EventInfo paramEventInfo, String paramString1, String paramString2) {
    super(paramEventInfo);
    this.oneTimeCode = paramString1;
    this.verificationId = paramString2;
  }
  
  public VerifyRegistrationRequest(EventInfo paramEventInfo, String paramString) {
    super(paramEventInfo);
    this.verificationId = paramString;
  }
}
