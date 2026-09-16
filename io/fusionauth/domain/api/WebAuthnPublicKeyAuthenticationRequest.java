package io.fusionauth.domain.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.webauthn.WebAuthnExtensionsClientOutputs;

public class WebAuthnPublicKeyAuthenticationRequest implements Buildable<WebAuthnPublicKeyAuthenticationRequest> {
  public WebAuthnExtensionsClientOutputs clientExtensionResults = new WebAuthnExtensionsClientOutputs();
  
  public String id;
  
  @JsonProperty("rpId")
  public String relyingPartyId;
  
  public WebAuthnAuthenticatorAuthenticationResponse response;
  
  public String type;
}
