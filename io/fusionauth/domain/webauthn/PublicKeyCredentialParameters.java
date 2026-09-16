package io.fusionauth.domain.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;

public class PublicKeyCredentialParameters implements Buildable<PublicKeyCredentialParameters> {
  @JsonProperty("alg")
  public CoseAlgorithmIdentifier algorithm;
  
  public PublicKeyCredentialType type = PublicKeyCredentialType.publicKey;
  
  @JacksonConstructor
  public PublicKeyCredentialParameters() {}
  
  public PublicKeyCredentialParameters(CoseAlgorithmIdentifier paramCoseAlgorithmIdentifier) {
    this.algorithm = paramCoseAlgorithmIdentifier;
  }
  
  public PublicKeyCredentialParameters(CoseAlgorithmIdentifier paramCoseAlgorithmIdentifier, PublicKeyCredentialType paramPublicKeyCredentialType) {
    this.algorithm = paramCoseAlgorithmIdentifier;
    this.type = paramPublicKeyCredentialType;
  }
}
