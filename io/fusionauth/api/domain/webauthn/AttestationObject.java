package io.fusionauth.api.domain.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;

public class AttestationObject implements Buildable<AttestationObject> {
  @JsonProperty("authData")
  public AuthenticatorData data;
  
  @JsonProperty("fmt")
  public AttestationFormatIdentifier formatId;
  
  @JsonProperty("attStmt")
  public AttestationStatement statement;
  
  @JacksonConstructor
  public AttestationObject() {}
  
  public AttestationObject(AttestationStatement paramAttestationStatement, AuthenticatorData paramAuthenticatorData, AttestationFormatIdentifier paramAttestationFormatIdentifier) {
    this.statement = paramAttestationStatement;
    this.data = paramAuthenticatorData;
    this.formatId = paramAttestationFormatIdentifier;
  }
}
