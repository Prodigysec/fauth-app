package io.fusionauth.domain.api;

import io.fusionauth.domain.APIKey;
import java.util.UUID;

public class APIKeyRequest {
  public APIKey apiKey;
  
  public UUID sourceKeyId;
  
  public APIKeyRequest() {}
  
  public APIKeyRequest(APIKey paramAPIKey) {
    this.apiKey = paramAPIKey;
  }
}
