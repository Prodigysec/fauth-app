package io.fusionauth.api.service.webauthn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.google.inject.Inject;
import com.inversoft.json.JacksonConstructor;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.webauthn.AttestationFormatIdentifier;
import io.fusionauth.api.domain.webauthn.AttestationObject;
import io.fusionauth.api.domain.webauthn.AttestedCredentialData;
import io.fusionauth.api.domain.webauthn.AuthenticatorAssertionResponse;
import io.fusionauth.api.domain.webauthn.AuthenticatorAttestationResponse;
import io.fusionauth.api.domain.webauthn.AuthenticatorData;
import io.fusionauth.api.domain.webauthn.CollectedClientData;
import io.fusionauth.api.domain.webauthn.CoseKey;
import io.fusionauth.api.domain.webauthn.ECCoseKey;
import io.fusionauth.api.domain.webauthn.PublicKeyCredential;
import io.fusionauth.domain.api.WebAuthnPublicKeyAuthenticationRequest;
import io.fusionauth.domain.api.WebAuthnPublicKeyRegistrationRequest;
import io.fusionauth.domain.webauthn.CoseAlgorithmIdentifier;
import io.fusionauth.domain.webauthn.PublicKeyCredentialType;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;

public class DefaultWebAuthnService implements WebAuthnService {
  private final CBORMapper cborMapper;
  
  private final ObjectMapper objectMapper;
  
  @Inject
  public DefaultWebAuthnService(CBORMapper paramCBORMapper, ObjectMapper paramObjectMapper) {
    this.cborMapper = paramCBORMapper;
    this.objectMapper = paramObjectMapper;
  }
  
  public PublicKeyCredential<AuthenticatorAssertionResponse> parseWebAuthnAuthenticationRequest(WebAuthnPublicKeyAuthenticationRequest paramWebAuthnPublicKeyAuthenticationRequest, Validator paramValidator) {
    paramValidator.notBlank(paramWebAuthnPublicKeyAuthenticationRequest.type, "credential.type", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(paramWebAuthnPublicKeyAuthenticationRequest.type.equals("public-key"), "credential.type", "[invalid]", new Object[] { Arrays.<PublicKeyCredentialType>stream(PublicKeyCredentialType.values()).map(()).collect(Collectors.joining(", ")) }));
    AuthenticatorData authenticatorData = null;
    CollectedClientData collectedClientData = parseClientData(paramValidator, paramWebAuthnPublicKeyAuthenticationRequest.response.clientDataJSON);
    try {
      authenticatorData = parseAuthenticatorData(Base64.getUrlDecoder().decode(paramWebAuthnPublicKeyAuthenticationRequest.response.authenticatorData));
    } catch (IOException iOException) {
      paramValidator.ensureWithCode(false, "credential.response.authenticatorData", "[invalid]credential.response.authenticatorData", new Object[] { String.format("The public key must be provided in valid COSE format. Error: %s", new Object[] { iOException.getMessage() }) });
    } 
    if (paramValidator.hasErrors())
      return null; 
    AuthenticatorAssertionResponse authenticatorAssertionResponse = new AuthenticatorAssertionResponse(collectedClientData, authenticatorData, paramWebAuthnPublicKeyAuthenticationRequest.response.signature, paramWebAuthnPublicKeyAuthenticationRequest.response.userHandle);
    return (new PublicKeyCredential<>())
      .with(paramPublicKeyCredential -> paramPublicKeyCredential.clientExtensionResults = paramWebAuthnPublicKeyAuthenticationRequest.clientExtensionResults)
      .with(paramPublicKeyCredential -> paramPublicKeyCredential.id = paramWebAuthnPublicKeyAuthenticationRequest.id)
      .with(paramPublicKeyCredential -> paramPublicKeyCredential.type = PublicKeyCredentialType.publicKey)
      .with(paramPublicKeyCredential -> paramPublicKeyCredential.rawId = Base64.getUrlDecoder().decode(paramPublicKeyCredential.id))
      .with(paramPublicKeyCredential -> paramPublicKeyCredential.response = (R)paramAuthenticatorAssertionResponse);
  }
  
  public PublicKeyCredential<AuthenticatorAttestationResponse> parseWebAuthnRegistrationRequest(WebAuthnPublicKeyRegistrationRequest paramWebAuthnPublicKeyRegistrationRequest, Validator paramValidator) {
    paramValidator.notBlank(paramWebAuthnPublicKeyRegistrationRequest.type, "credential.type", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(paramWebAuthnPublicKeyRegistrationRequest.type.equals("public-key"), "credential.type", "[invalid]", new Object[] { Arrays.<PublicKeyCredentialType>stream(PublicKeyCredentialType.values()).map(()).collect(Collectors.joining(", ")) }));
    AttestationObjectCbor attestationObjectCbor = null;
    AuthenticatorData authenticatorData = null;
    CollectedClientData collectedClientData = parseClientData(paramValidator, paramWebAuthnPublicKeyRegistrationRequest.response.clientDataJSON);
    try {
      attestationObjectCbor = (AttestationObjectCbor)this.cborMapper.readValue(Base64.getUrlDecoder().decode(paramWebAuthnPublicKeyRegistrationRequest.response.attestationObject), AttestationObjectCbor.class);
      authenticatorData = parseAuthenticatorData(attestationObjectCbor.authData);
    } catch (Exception exception) {
      paramValidator
        .validObject(attestationObjectCbor, "credential.response.attestationObject", new Object[] { String.format("The value does not contain valid CBOR-encoded data. Error: %s", new Object[] { exception.getMessage() }) }).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(false, "credential.response.attestationObject", "[invalid]", new Object[] { String.format("The public key must be provided in valid COSE format. Error: %s", new Object[] { paramException.getMessage() }) }));
    } 
    validateCredentialData(authenticatorData, paramValidator);
    if (paramValidator.hasErrors() || attestationObjectCbor == null)
      return null; 
    AttestationObject attestationObject = new AttestationObject(null, authenticatorData, attestationObjectCbor.fmt);
    AuthenticatorAttestationResponse authenticatorAttestationResponse = new AuthenticatorAttestationResponse(collectedClientData, attestationObject);
    return (new PublicKeyCredential<>())
      .with(paramPublicKeyCredential -> paramPublicKeyCredential.clientExtensionResults = paramWebAuthnPublicKeyRegistrationRequest.clientExtensionResults)
      .with(paramPublicKeyCredential -> paramPublicKeyCredential.id = paramWebAuthnPublicKeyRegistrationRequest.id)
      .with(paramPublicKeyCredential -> paramPublicKeyCredential.type = PublicKeyCredentialType.publicKey)
      .with(paramPublicKeyCredential -> paramPublicKeyCredential.rawId = Base64.getUrlDecoder().decode(paramPublicKeyCredential.id))
      .with(paramPublicKeyCredential -> paramPublicKeyCredential.response = (R)paramAuthenticatorAttestationResponse)
      .with(paramPublicKeyCredential -> paramPublicKeyCredential.transports = paramWebAuthnPublicKeyRegistrationRequest.transports);
  }
  
  public String serializeAttestationObject(AttestationObject paramAttestationObject) throws JsonProcessingException {
    AttestationObjectCbor attestationObjectCbor = new AttestationObjectCbor(paramAttestationObject.statement, serializeAuthenticatorData(paramAttestationObject.data), paramAttestationObject.formatId);
    return Base64.getUrlEncoder().encodeToString((new CBORMapper()).writeValueAsBytes(attestationObjectCbor));
  }
  
  public byte[] serializeAuthenticatorData(AuthenticatorData paramAuthenticatorData) throws JsonProcessingException {
    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    byteBuffer.put(paramAuthenticatorData.rpIdHash);
    byteBuffer.put(paramAuthenticatorData.flags);
    byteBuffer.putInt(paramAuthenticatorData.signCount);
    if (paramAuthenticatorData.hasCredentialData()) {
      byteBuffer.putLong(paramAuthenticatorData.attestedCredentialData.authenticatorAttestationGuid.getMostSignificantBits());
      byteBuffer.putLong(paramAuthenticatorData.attestedCredentialData.authenticatorAttestationGuid.getLeastSignificantBits());
      byteBuffer.putShort(paramAuthenticatorData.attestedCredentialData.credentialIdLength);
      byteBuffer.put(paramAuthenticatorData.attestedCredentialData.credentialId);
      byteBuffer.put(this.cborMapper.writeValueAsBytes(paramAuthenticatorData.attestedCredentialData.credentialPublicKey));
    } 
    byte[] arrayOfByte = new byte[byteBuffer.position()];
    byteBuffer.rewind();
    byteBuffer.get(arrayOfByte);
    return arrayOfByte;
  }
  
  private AuthenticatorData parseAuthenticatorData(byte[] paramArrayOfbyte) throws IOException {
    ByteBuffer byteBuffer = ByteBuffer.wrap(paramArrayOfbyte);
    byte[] arrayOfByte = new byte[32];
    byteBuffer.get(arrayOfByte);
    byte b = byteBuffer.get();
    int i = byteBuffer.getInt();
    AuthenticatorData authenticatorData = (new AuthenticatorData()).with(paramAuthenticatorData -> paramAuthenticatorData.rpIdHash = paramArrayOfbyte).with(paramAuthenticatorData -> paramAuthenticatorData.flags = paramByte).with(paramAuthenticatorData -> paramAuthenticatorData.signCount = paramInt);
    if (authenticatorData.hasCredentialData()) {
      long l1 = byteBuffer.getLong();
      long l2 = byteBuffer.getLong();
      short s = byteBuffer.getShort();
      byte[] arrayOfByte1 = new byte[s];
      byteBuffer.get(arrayOfByte1);
      byte[] arrayOfByte2 = new byte[byteBuffer.remaining()];
      byteBuffer.get(arrayOfByte2);
      CoseKey coseKey = (CoseKey)this.cborMapper.readValue(arrayOfByte2, CoseKey.class);
      authenticatorData


        
        .attestedCredentialData = (new AttestedCredentialData()).with(paramAttestedCredentialData -> paramAttestedCredentialData.authenticatorAttestationGuid = new UUID(paramLong1, paramLong2)).with(paramAttestedCredentialData -> paramAttestedCredentialData.credentialIdLength = paramShort).with(paramAttestedCredentialData -> paramAttestedCredentialData.credentialId = paramArrayOfbyte).with(paramAttestedCredentialData -> paramAttestedCredentialData.credentialPublicKey = paramCoseKey);
    } 
    return authenticatorData;
  }
  
  private CollectedClientData parseClientData(Validator paramValidator, String paramString) {
    try {
      return (CollectedClientData)this.objectMapper.readValue(Base64.getUrlDecoder().decode(paramString), CollectedClientData.class);
    } catch (Exception exception) {
      paramValidator.ensure(false, "credential.response.clientDataJSON", "[invalid]", new Object[] { String.format("The encoded client data does not contain valid JSON. Error: %s", new Object[] { exception.getMessage() }) });
      return null;
    } 
  }
  
  private void validateCredentialData(AuthenticatorData paramAuthenticatorData, Validator paramValidator) {
    if (paramAuthenticatorData == null || 
      !paramAuthenticatorData.hasCredentialData() || paramAuthenticatorData.attestedCredentialData.credentialPublicKey == null || paramAuthenticatorData.attestedCredentialData.credentialPublicKey.algorithm == null)
      return; 
    CoseKey coseKey = paramAuthenticatorData.attestedCredentialData.credentialPublicKey;
    CoseAlgorithmIdentifier coseAlgorithmIdentifier = coseKey.algorithm;
    paramValidator.ensure((coseKey.keyType() == coseAlgorithmIdentifier.keyType), "credential.response.attestationObject", "[invalid]", new Object[] { "The signature algorithm does not support the key type of the provided public key." });
    if (coseKey instanceof ECCoseKey) {
      ECCoseKey eCCoseKey = (ECCoseKey)coseKey;
      paramValidator.ensure((eCCoseKey.curveId == coseAlgorithmIdentifier.getCurve()), "credential.response.attestationObject", "[invalid]", new Object[] { "The signature algorithm does not support the underlying elliptic curve of the provided public key." });
    } 
  }
  
  public static class AttestationObjectCbor {
    public Object attStmt;
    
    public byte[] authData;
    
    public AttestationFormatIdentifier fmt;
    
    @JacksonConstructor
    public AttestationObjectCbor() {}
    
    public AttestationObjectCbor(Object param1Object, byte[] param1ArrayOfbyte, AttestationFormatIdentifier param1AttestationFormatIdentifier) {
      this.attStmt = param1Object;
      this.authData = param1ArrayOfbyte;
      this.fmt = param1AttestationFormatIdentifier;
    }
  }
}
