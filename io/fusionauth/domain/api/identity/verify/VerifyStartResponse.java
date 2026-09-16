package io.fusionauth.domain.api.identity.verify;

import com.inversoft.json.JacksonConstructor;

public class VerifyStartResponse {
  public String oneTimeCode;
  
  public String verificationId;
  
  public VerifyStartResponse(String paramString1, String paramString2) {
    this.oneTimeCode = paramString1;
    this.verificationId = paramString2;
  }
  
  @JacksonConstructor
  private VerifyStartResponse() {}
}
