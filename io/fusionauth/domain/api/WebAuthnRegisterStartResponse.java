package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.webauthn.PublicKeyCredentialCreationOptions;

public class WebAuthnRegisterStartResponse {
  public PublicKeyCredentialCreationOptions options;
  
  @JacksonConstructor
  public WebAuthnRegisterStartResponse() {}
  
  public WebAuthnRegisterStartResponse(PublicKeyCredentialCreationOptions paramPublicKeyCredentialCreationOptions) {
    this.options = paramPublicKeyCredentialCreationOptions;
  }
}
