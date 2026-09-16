package io.fusionauth.api.domain.webauthn;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;

public class AuthenticatorAssertionResponse extends AuthenticatorResponse implements Buildable<AuthenticatorAssertionResponse> {
  public AuthenticatorData authenticatorData;
  
  public String signature;
  
  public String userHandle;
  
  @JacksonConstructor
  public AuthenticatorAssertionResponse() {}
  
  public AuthenticatorAssertionResponse(CollectedClientData paramCollectedClientData, AuthenticatorData paramAuthenticatorData, String paramString1, String paramString2) {
    super(paramCollectedClientData);
    this.authenticatorData = paramAuthenticatorData;
    this.signature = paramString1;
    this.userHandle = paramString2;
  }
}
