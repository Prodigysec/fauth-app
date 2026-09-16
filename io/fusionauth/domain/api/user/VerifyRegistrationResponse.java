package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;

public class VerifyRegistrationResponse {
  public String oneTimeCode;
  
  public String verificationId;
  
  @JacksonConstructor
  public VerifyRegistrationResponse() {}
  
  public VerifyRegistrationResponse(String paramString) {
    this.verificationId = paramString;
  }
  
  public VerifyRegistrationResponse(String paramString1, String paramString2) {
    this.oneTimeCode = paramString1;
    this.verificationId = paramString2;
  }
}
