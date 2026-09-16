package io.fusionauth.domain.api;

public class ReactorRequest {
  public String license;
  
  public String licenseId;
  
  public ReactorRequest() {}
  
  public ReactorRequest(String paramString1, String paramString2) {
    this.licenseId = paramString1;
    this.license = paramString2;
  }
}
