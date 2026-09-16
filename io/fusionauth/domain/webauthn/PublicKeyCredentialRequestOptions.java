package io.fusionauth.domain.webauthn;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fusionauth.domain.Buildable;
import java.util.List;

public class PublicKeyCredentialRequestOptions implements Buildable<PublicKeyCredentialRequestOptions> {
  public List<PublicKeyCredentialDescriptor> allowCredentials;
  
  public String challenge;
  
  @JsonProperty("rpId")
  public String relyingPartyId;
  
  public long timeout;
  
  public UserVerificationRequirement userVerification;
}
