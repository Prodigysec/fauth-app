package io.fusionauth.domain.api.twoFactor;

import com.inversoft.json.JacksonConstructor;

public class SecretResponse {
  public String secret;
  
  public String secretBase32Encoded;
  
  @JacksonConstructor
  public SecretResponse() {}
  
  public SecretResponse(String paramString1, String paramString2) {
    this.secret = paramString1;
    this.secretBase32Encoded = paramString2;
  }
}
