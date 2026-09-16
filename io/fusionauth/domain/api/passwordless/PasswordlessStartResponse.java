package io.fusionauth.domain.api.passwordless;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;

public class PasswordlessStartResponse implements Buildable<PasswordlessStartResponse> {
  public String code;
  
  public String oneTimeCode;
  
  @JacksonConstructor
  public PasswordlessStartResponse() {}
  
  public PasswordlessStartResponse(String paramString) {
    this.code = paramString;
  }
  
  public PasswordlessStartResponse(String paramString1, String paramString2) {
    this.code = paramString1;
    this.oneTimeCode = paramString2;
  }
}
