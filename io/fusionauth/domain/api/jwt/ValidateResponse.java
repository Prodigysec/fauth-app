package io.fusionauth.domain.api.jwt;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.jwt.domain.JWT;

public class ValidateResponse {
  public JWT jwt;
  
  public ValidateResponse(JWT paramJWT) {
    this.jwt = paramJWT;
  }
  
  @JacksonConstructor
  public ValidateResponse() {}
}
