package io.fusionauth.api.domain.webauthn;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.webauthn.WebAuthnExtensionsClientOutputs;
import java.util.ArrayList;
import java.util.List;

public class PublicKeyCredential<R extends AuthenticatorResponse> extends Credential implements Buildable<PublicKeyCredential<R>> {
  public WebAuthnExtensionsClientOutputs clientExtensionResults;
  
  public byte[] rawId;
  
  public R response;
  
  public List<String> transports = new ArrayList<>();
}
