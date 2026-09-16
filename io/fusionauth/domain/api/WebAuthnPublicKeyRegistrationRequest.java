package io.fusionauth.domain.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.webauthn.WebAuthnExtensionsClientOutputs;
import java.util.ArrayList;
import java.util.List;

public class WebAuthnPublicKeyRegistrationRequest implements Buildable<WebAuthnPublicKeyRegistrationRequest> {
  public WebAuthnExtensionsClientOutputs clientExtensionResults = new WebAuthnExtensionsClientOutputs();
  
  public String id;
  
  @JsonProperty("rpId")
  public String relyingPartyId;
  
  public WebAuthnAuthenticatorRegistrationResponse response;
  
  public List<String> transports = new ArrayList<>();
  
  public String type;
}
