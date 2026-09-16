package io.fusionauth.api.domain.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fusionauth.domain.Buildable;
import java.util.UUID;

public class AttestedCredentialData implements Buildable<AttestedCredentialData> {
  @JsonProperty("aaguid")
  public UUID authenticatorAttestationGuid;
  
  public byte[] credentialId;
  
  public short credentialIdLength;
  
  public CoseKey credentialPublicKey;
}
