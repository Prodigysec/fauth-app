package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import java.util.List;

public class TwoFactorResponse implements Buildable<TwoFactorResponse> {
  public String code;
  
  public List<String> recoveryCodes;
  
  @JacksonConstructor
  public TwoFactorResponse() {}
  
  public TwoFactorResponse(List<String> paramList) {
    this.recoveryCodes = paramList;
  }
}
