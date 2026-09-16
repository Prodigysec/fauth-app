package io.fusionauth.api.domain.webauthn;

import io.fusionauth.domain.webauthn.PublicKeyCredentialType;

public abstract class Credential {
  public String id;
  
  public PublicKeyCredentialType type;
}
