package io.fusionauth.domain.webauthn;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WebAuthnExtensionsClientOutputs {
  public CredentialPropertiesOutput credProps = new CredentialPropertiesOutput();
  
  @JsonIgnore
  public boolean isDiscoverableCredential() {
    return (this.credProps != null && this.credProps.rk);
  }
}
