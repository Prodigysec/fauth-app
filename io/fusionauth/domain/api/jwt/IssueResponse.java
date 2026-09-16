package io.fusionauth.domain.api.jwt;

import com.inversoft.json.JacksonConstructor;

public class IssueResponse {
  public String refreshToken;
  
  public String token;
  
  @JacksonConstructor
  public IssueResponse() {}
  
  public IssueResponse(String paramString) {
    this.token = paramString;
  }
}
