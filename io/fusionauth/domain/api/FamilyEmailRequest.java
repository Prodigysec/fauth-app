package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;

public class FamilyEmailRequest {
  public String parentEmail;
  
  public FamilyEmailRequest(String paramString) {
    this.parentEmail = paramString;
  }
  
  @JacksonConstructor
  public FamilyEmailRequest() {}
}
