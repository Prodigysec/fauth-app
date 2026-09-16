package io.fusionauth.api.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.api.domain.ExternalIdentifier;

public class ExternalIdentifierResponse {
  public ExternalIdentifier externalIdentifier;
  
  @JacksonConstructor
  public ExternalIdentifierResponse() {}
  
  public ExternalIdentifierResponse(ExternalIdentifier paramExternalIdentifier) {
    this.externalIdentifier = paramExternalIdentifier;
  }
}
