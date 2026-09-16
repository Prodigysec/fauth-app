package io.fusionauth.domain.api.jwt;

import com.inversoft.json.JacksonConstructor;

public class JWTVendResponse {
  public String token;
  
  @JacksonConstructor
  public JWTVendResponse() {}
  
  public JWTVendResponse(String paramString) {
    this.token = paramString;
  }
}
