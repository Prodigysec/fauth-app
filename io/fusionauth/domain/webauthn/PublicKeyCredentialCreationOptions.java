package io.fusionauth.domain.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fusionauth.domain.Buildable;
import java.util.List;

public class PublicKeyCredentialCreationOptions implements Buildable<PublicKeyCredentialCreationOptions> {
  public AttestationConveyancePreference attestation = AttestationConveyancePreference.none;
  
  public AuthenticatorSelectionCriteria authenticatorSelection;
  
  public String challenge;
  
  public List<PublicKeyCredentialDescriptor> excludeCredentials;
  
  public WebAuthnRegistrationExtensionOptions extensions;
  
  public List<PublicKeyCredentialParameters> pubKeyCredParams;
  
  @JsonProperty("rp")
  public PublicKeyCredentialRelyingPartyEntity relyingParty;
  
  public long timeout;
  
  public PublicKeyCredentialUserEntity user;
}
