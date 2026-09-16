package io.fusionauth.domain.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fusionauth.domain.Buildable;
import java.util.UUID;

public class WebAuthnRegisterCompleteRequest implements Buildable<WebAuthnRegisterCompleteRequest> {
  public WebAuthnPublicKeyRegistrationRequest credential;
  
  @Deprecated
  public String origin;
  
  @Deprecated
  @JsonProperty("rpId")
  public String relyingPartyId;
  
  public UUID userId;
}
