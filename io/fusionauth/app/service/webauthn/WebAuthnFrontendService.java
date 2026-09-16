package io.fusionauth.app.service.webauthn;

import io.fusionauth.domain.api.WebAuthnLoginRequest;
import io.fusionauth.domain.api.WebAuthnRegisterCompleteRequest;
import java.util.UUID;

public interface WebAuthnFrontendService {
  WebAuthnLoginRequest unmarshalWebAuthnLoginRequest(String paramString);
  
  WebAuthnRegisterCompleteRequest unmarshalWebAuthnRegistrationRequest(String paramString, UUID paramUUID);
}
