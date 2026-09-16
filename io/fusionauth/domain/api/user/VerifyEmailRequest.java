package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.api.BaseEventRequest;
import java.util.UUID;

public class VerifyEmailRequest extends BaseEventRequest {
  public String oneTimeCode;
  
  public UUID userId;
  
  public String verificationId;
  
  @JacksonConstructor
  public VerifyEmailRequest() {}
  
  public VerifyEmailRequest(String paramString1, String paramString2) {
    this.oneTimeCode = paramString1;
    this.verificationId = paramString2;
  }
  
  public VerifyEmailRequest(String paramString) {
    this.verificationId = paramString;
  }
  
  public VerifyEmailRequest(EventInfo paramEventInfo, String paramString1, String paramString2) {
    super(paramEventInfo);
    this.oneTimeCode = paramString1;
    this.verificationId = paramString2;
  }
  
  public VerifyEmailRequest(EventInfo paramEventInfo, String paramString) {
    super(paramEventInfo);
    this.verificationId = paramString;
  }
  
  public VerifyEmailRequest(UUID paramUUID) {
    this.userId = paramUUID;
  }
}
