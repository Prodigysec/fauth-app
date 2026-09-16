package io.fusionauth.domain.api;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.WebAuthnCredential;
import java.util.List;

public class WebAuthnCredentialImportRequest implements Buildable<WebAuthnCredentialImportRequest> {
  public List<WebAuthnCredential> credentials;
  
  public boolean validateDbConstraints;
}
