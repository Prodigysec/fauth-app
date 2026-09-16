package io.fusionauth.api.domain.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AttestationStatement {
  @JsonProperty("x5c")
  public byte[][] certificateChain;
  
  @JsonProperty("sig")
  public byte[] signature;
}
