package io.fusionauth.domain.api;

import io.fusionauth.domain.Buildable;

public class WebAuthnAuthenticatorRegistrationResponse implements Buildable<WebAuthnAuthenticatorRegistrationResponse> {
  public String attestationObject;
  
  public String clientDataJSON;
}
