package io.fusionauth.domain.api;

import io.fusionauth.domain.Buildable;

public class WebAuthnAuthenticatorAuthenticationResponse implements Buildable<WebAuthnAuthenticatorAuthenticationResponse> {
  public String authenticatorData;
  
  public String clientDataJSON;
  
  public String signature;
  
  public String userHandle;
}
