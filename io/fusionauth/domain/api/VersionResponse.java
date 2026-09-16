package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;

public class VersionResponse {
  public String version;
  
  public VersionResponse(String paramString) {
    this.version = paramString;
  }
  
  @JacksonConstructor
  public VersionResponse() {}
}
