package io.fusionauth.domain.webauthn;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.WebAuthnCredential;
import java.util.List;

public class PublicKeyCredentialDescriptor implements Buildable<PublicKeyCredentialDescriptor> {
  public String id;
  
  public List<String> transports;
  
  public PublicKeyCredentialType type = PublicKeyCredentialType.publicKey;
  
  @JacksonConstructor
  public PublicKeyCredentialDescriptor() {}
  
  public PublicKeyCredentialDescriptor(WebAuthnCredential paramWebAuthnCredential) {
    this.id = paramWebAuthnCredential.credentialId;
    this.transports = paramWebAuthnCredential.transports;
  }
}
