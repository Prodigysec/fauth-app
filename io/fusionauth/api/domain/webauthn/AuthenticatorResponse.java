package io.fusionauth.api.domain.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inversoft.json.JacksonConstructor;

public abstract class AuthenticatorResponse {
  @JsonProperty("clientDataJSON")
  public CollectedClientData clientData;
  
  @JacksonConstructor
  public AuthenticatorResponse() {}
  
  public AuthenticatorResponse(CollectedClientData paramCollectedClientData) {
    this.clientData = paramCollectedClientData;
  }
}
