package io.fusionauth.domain.api;

import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.webauthn.WebAuthnWorkflow;
import java.util.UUID;

public class WebAuthnRegisterStartRequest implements Buildable<WebAuthnRegisterStartRequest> {
  public String displayName;
  
  public String name;
  
  public String userAgent;
  
  public UUID userId;
  
  public WebAuthnWorkflow workflow;
}
