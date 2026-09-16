package io.fusionauth.api.domain.webauthn;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;

public class AuthenticatorAttestationResponse extends AuthenticatorResponse implements Buildable<AuthenticatorAttestationResponse> {
  public AttestationObject attestationObject;
  
  @JacksonConstructor
  public AuthenticatorAttestationResponse() {}
  
  public AuthenticatorAttestationResponse(CollectedClientData paramCollectedClientData, AttestationObject paramAttestationObject) {
    super(paramCollectedClientData);
    this.attestationObject = paramAttestationObject;
  }
}
