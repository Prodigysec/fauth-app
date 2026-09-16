package io.fusionauth.domain.api.twoFactor;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.TwoFactorMethod;
import java.util.List;

public class TwoFactorStartResponse implements Buildable<TwoFactorStartResponse> {
  public String code;
  
  public List<TwoFactorMethod> methods;
  
  public String twoFactorId;
  
  @JacksonConstructor
  public TwoFactorStartResponse() {}
  
  public TwoFactorStartResponse(String paramString1, List<TwoFactorMethod> paramList, String paramString2) {
    this.code = paramString1;
    this.methods = paramList;
    this.twoFactorId = paramString2;
  }
}
