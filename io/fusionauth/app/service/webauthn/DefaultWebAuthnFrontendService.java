package io.fusionauth.app.service.webauthn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.util.EncoderTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.api.WebAuthnLoginRequest;
import io.fusionauth.domain.api.WebAuthnPublicKeyAuthenticationRequest;
import io.fusionauth.domain.api.WebAuthnPublicKeyRegistrationRequest;
import io.fusionauth.domain.api.WebAuthnRegisterCompleteRequest;
import java.util.UUID;

public class DefaultWebAuthnFrontendService implements WebAuthnFrontendService {
  private final FrontEndSupport frontEndSupport;
  
  @Inject
  public DefaultWebAuthnFrontendService(FrontEndSupport paramFrontEndSupport) {
    this.frontEndSupport = paramFrontEndSupport;
  }
  
  public WebAuthnLoginRequest unmarshalWebAuthnLoginRequest(String paramString) {
    if (paramString == null)
      return null; 
    try {
      WebAuthnPublicKeyAuthenticationRequest webAuthnPublicKeyAuthenticationRequest = (WebAuthnPublicKeyAuthenticationRequest)this.frontEndSupport.objectMapper.readValue(EncoderTools.Base64.decodeToString(paramString), WebAuthnPublicKeyAuthenticationRequest.class);
      String str = (webAuthnPublicKeyAuthenticationRequest.relyingPartyId != null) ? webAuthnPublicKeyAuthenticationRequest.relyingPartyId : this.frontEndSupport.request.getHost();
      return (new WebAuthnLoginRequest())
        .with(paramWebAuthnLoginRequest -> paramWebAuthnLoginRequest.credential = paramWebAuthnPublicKeyAuthenticationRequest)
        .with(paramWebAuthnLoginRequest -> paramWebAuthnLoginRequest.origin = this.frontEndSupport.request.getBaseURL())
        .with(paramWebAuthnLoginRequest -> paramWebAuthnLoginRequest.relyingPartyId = paramString);
    } catch (IllegalArgumentException illegalArgumentException) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to decode the WebAuthn public key authentication request.\nEncoded request:\n" + paramString, illegalArgumentException));
    } catch (JsonProcessingException jsonProcessingException) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to deserialize the WebAuthn public key authentication request.\nSerialized request:\n" + EncoderTools.Base64.decodeToString(paramString), (Throwable)jsonProcessingException));
    } 
    return null;
  }
  
  public WebAuthnRegisterCompleteRequest unmarshalWebAuthnRegistrationRequest(String paramString, UUID paramUUID) {
    if (paramString == null)
      return null; 
    try {
      WebAuthnPublicKeyRegistrationRequest webAuthnPublicKeyRegistrationRequest = (WebAuthnPublicKeyRegistrationRequest)this.frontEndSupport.objectMapper.readValue(EncoderTools.Base64.decodeToString(paramString), WebAuthnPublicKeyRegistrationRequest.class);
      String str = (webAuthnPublicKeyRegistrationRequest.relyingPartyId != null) ? webAuthnPublicKeyRegistrationRequest.relyingPartyId : this.frontEndSupport.request.getHost();
      return (new WebAuthnRegisterCompleteRequest())
        .with(paramWebAuthnRegisterCompleteRequest -> paramWebAuthnRegisterCompleteRequest.userId = paramUUID)
        .with(paramWebAuthnRegisterCompleteRequest -> paramWebAuthnRegisterCompleteRequest.credential = paramWebAuthnPublicKeyRegistrationRequest)
        .with(paramWebAuthnRegisterCompleteRequest -> paramWebAuthnRegisterCompleteRequest.origin = this.frontEndSupport.request.getBaseURL())
        .with(paramWebAuthnRegisterCompleteRequest -> paramWebAuthnRegisterCompleteRequest.relyingPartyId = paramString);
    } catch (IllegalArgumentException illegalArgumentException) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to decode the WebAuthn public key registration request.\nEncoded request:\n" + paramString, illegalArgumentException));
    } catch (JsonProcessingException jsonProcessingException) {
      EventLogHelper.create(new EventLog(EventLogType.Error, "Failed to deserialize the WebAuthn public key registration request.\nSerialized request:\n" + EncoderTools.Base64.decodeToString(paramString), (Throwable)jsonProcessingException));
    } 
    return null;
  }
}
