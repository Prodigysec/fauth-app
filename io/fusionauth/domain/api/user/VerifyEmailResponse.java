package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;

public class VerifyEmailResponse {
  public String oneTimeCode;
  
  public String verificationId;
  
  @JacksonConstructor
  public VerifyEmailResponse() {}
  
  public VerifyEmailResponse(String paramString1, String paramString2) {
    this.oneTimeCode = paramString1;
    this.verificationId = paramString2;
  }
  
  public VerifyEmailResponse(String paramString) {
    this.verificationId = paramString;
  }
}
