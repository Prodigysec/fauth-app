package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.WebAuthnCredential;

public class WebAuthnRegisterCompleteResponse implements Buildable<WebAuthnRegisterCompleteResponse> {
  public WebAuthnCredential credential;
  
  @JacksonConstructor
  public WebAuthnRegisterCompleteResponse() {}
  
  public WebAuthnRegisterCompleteResponse(WebAuthnCredential paramWebAuthnCredential) {
    this.credential = paramWebAuthnCredential;
  }
}
