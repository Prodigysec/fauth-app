package io.fusionauth.domain.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fusionauth.domain.Buildable;

public class WebAuthnLoginRequest extends BaseLoginRequest implements Buildable<WebAuthnLoginRequest> {
  public WebAuthnPublicKeyAuthenticationRequest credential;
  
  @Deprecated
  public String origin;
  
  @Deprecated
  @JsonProperty("rpId")
  public String relyingPartyId;
  
  public String twoFactorTrustId;
}
