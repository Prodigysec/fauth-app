package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import java.util.Map;

public class ChangePasswordResponse {
  public String oneTimePassword;
  
  public Map<String, Object> state;
  
  @JacksonConstructor
  public ChangePasswordResponse() {}
  
  public ChangePasswordResponse(String paramString) {
    this.oneTimePassword = paramString;
  }
  
  public ChangePasswordResponse(String paramString, Map<String, Object> paramMap) {
    this.oneTimePassword = paramString;
    this.state = paramMap;
  }
}
