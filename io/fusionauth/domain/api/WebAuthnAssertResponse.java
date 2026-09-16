package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.WebAuthnCredential;

public class WebAuthnAssertResponse implements Buildable<WebAuthnAssertResponse> {
  public WebAuthnCredential credential;
  
  @JacksonConstructor
  public WebAuthnAssertResponse() {}
  
  public WebAuthnAssertResponse(WebAuthnCredential paramWebAuthnCredential) {
    this.credential = paramWebAuthnCredential;
  }
}
