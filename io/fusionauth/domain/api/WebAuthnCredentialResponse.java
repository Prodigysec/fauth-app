package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.WebAuthnCredential;
import java.util.List;

public class WebAuthnCredentialResponse {
  public WebAuthnCredential credential;
  
  public List<WebAuthnCredential> credentials;
  
  @JacksonConstructor
  public WebAuthnCredentialResponse() {}
  
  public WebAuthnCredentialResponse(WebAuthnCredential paramWebAuthnCredential) {
    this.credential = paramWebAuthnCredential;
  }
  
  public WebAuthnCredentialResponse(List<WebAuthnCredential> paramList) {
    this.credentials = paramList;
  }
}
