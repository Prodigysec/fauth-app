package io.fusionauth.api.service.webauthn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.webauthn.AttestationObject;
import io.fusionauth.api.domain.webauthn.AuthenticatorAssertionResponse;
import io.fusionauth.api.domain.webauthn.AuthenticatorAttestationResponse;
import io.fusionauth.api.domain.webauthn.AuthenticatorData;
import io.fusionauth.api.domain.webauthn.PublicKeyCredential;
import io.fusionauth.domain.api.WebAuthnPublicKeyAuthenticationRequest;
import io.fusionauth.domain.api.WebAuthnPublicKeyRegistrationRequest;

public interface WebAuthnService {
  PublicKeyCredential<AuthenticatorAssertionResponse> parseWebAuthnAuthenticationRequest(WebAuthnPublicKeyAuthenticationRequest paramWebAuthnPublicKeyAuthenticationRequest, Validator paramValidator);
  
  PublicKeyCredential<AuthenticatorAttestationResponse> parseWebAuthnRegistrationRequest(WebAuthnPublicKeyRegistrationRequest paramWebAuthnPublicKeyRegistrationRequest, Validator paramValidator);
  
  String serializeAttestationObject(AttestationObject paramAttestationObject) throws JsonProcessingException;
  
  byte[] serializeAuthenticatorData(AuthenticatorData paramAuthenticatorData) throws JsonProcessingException;
}
