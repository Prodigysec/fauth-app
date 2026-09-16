package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.webauthn.WebAuthnWorkflow;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WebAuthnStartRequest implements Buildable<WebAuthnStartRequest> {
  public UUID applicationId;
  
  public UUID credentialId;
  
  public String loginId;
  
  public List<String> loginIdTypes = new ArrayList<>();
  
  public Map<String, Object> state;
  
  public UUID userId;
  
  public WebAuthnWorkflow workflow;
  
  @JacksonConstructor
  public WebAuthnStartRequest() {}
  
  public WebAuthnStartRequest(UUID paramUUID1, UUID paramUUID2, String paramString, UUID paramUUID3, Map<String, Object> paramMap, WebAuthnWorkflow paramWebAuthnWorkflow) {
    this.applicationId = paramUUID1;
    this.credentialId = paramUUID3;
    this.loginId = paramString;
    this.state = paramMap;
    this.userId = paramUUID2;
    this.workflow = paramWebAuthnWorkflow;
  }
}
