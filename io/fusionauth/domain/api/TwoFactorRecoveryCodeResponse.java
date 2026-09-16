package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import java.util.List;

public class TwoFactorRecoveryCodeResponse {
  public List<String> recoveryCodes;
  
  @JacksonConstructor
  public TwoFactorRecoveryCodeResponse() {}
  
  public TwoFactorRecoveryCodeResponse(List<String> paramList) {
    this.recoveryCodes = paramList;
  }
}
