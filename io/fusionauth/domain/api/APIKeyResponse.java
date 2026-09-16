package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.APIKey;

public class APIKeyResponse {
  public APIKey apiKey;
  
  @JacksonConstructor
  public APIKeyResponse() {}
  
  public APIKeyResponse(APIKey paramAPIKey) {
    this.apiKey = paramAPIKey;
  }
}
