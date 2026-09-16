package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;

public class ForgotPasswordResponse {
  public String changePasswordId;
  
  @JacksonConstructor
  public ForgotPasswordResponse() {}
  
  public ForgotPasswordResponse(String paramString) {
    this.changePasswordId = paramString;
  }
}
