package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;

public class TwoFactorEnableDisableSendRequest implements Buildable<TwoFactorEnableDisableSendRequest> {
  public String email;
  
  public String method;
  
  public String methodId;
  
  public String mobilePhone;
  
  @JacksonConstructor
  public TwoFactorEnableDisableSendRequest() {}
  
  public TwoFactorEnableDisableSendRequest(String paramString) {
    this.methodId = paramString;
  }
  
  public TwoFactorEnableDisableSendRequest(String paramString1, String paramString2, String paramString3) {
    this.method = paramString1;
    this.email = paramString2;
    this.mobilePhone = paramString3;
  }
}
