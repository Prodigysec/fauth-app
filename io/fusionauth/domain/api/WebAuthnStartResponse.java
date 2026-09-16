package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.webauthn.PublicKeyCredentialRequestOptions;

public class WebAuthnStartResponse {
  public PublicKeyCredentialRequestOptions options;
  
  @JacksonConstructor
  public WebAuthnStartResponse() {}
  
  public WebAuthnStartResponse(PublicKeyCredentialRequestOptions paramPublicKeyCredentialRequestOptions) {
    this.options = paramPublicKeyCredentialRequestOptions;
  }
}
