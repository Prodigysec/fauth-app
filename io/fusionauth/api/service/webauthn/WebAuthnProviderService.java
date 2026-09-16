package io.fusionauth.api.service.webauthn;

import com.inversoft.error.Errors;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.webauthn.AuthenticatorAssertionResponse;
import io.fusionauth.api.domain.webauthn.AuthenticatorAttestationResponse;
import io.fusionauth.api.domain.webauthn.PublicKeyCredential;
import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.WebAuthnCredential;
import io.fusionauth.domain.api.WebAuthnPublicKeyAuthenticationRequest;
import io.fusionauth.domain.api.WebAuthnPublicKeyRegistrationRequest;
import io.fusionauth.domain.webauthn.PublicKeyCredentialCreationOptions;
import io.fusionauth.domain.webauthn.PublicKeyCredentialRequestOptions;
import io.fusionauth.domain.webauthn.WebAuthnWorkflow;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface WebAuthnProviderService {
  PublicKeyCredentialCreationOptions buildCreationOptionsResponse(Tenant paramTenant, User paramUser, WebAuthnWorkflow paramWebAuthnWorkflow, String paramString1, String paramString2, String paramString3);
  
  PublicKeyCredentialRequestOptions buildRequestOptionsResponse(Tenant paramTenant, Application paramApplication, User paramUser, UserIdentity paramUserIdentity, List<WebAuthnCredential> paramList, Map<String, Object> paramMap, WebAuthnWorkflow paramWebAuthnWorkflow);
  
  WebAuthnCredential completeAssertion(Tenant paramTenant, User paramUser, WebAuthnCredential paramWebAuthnCredential, PublicKeyCredential<AuthenticatorAssertionResponse> paramPublicKeyCredential, byte[] paramArrayOfbyte);
  
  WebAuthnCredential completeRegistration(Tenant paramTenant, User paramUser, PublicKeyCredential<AuthenticatorAttestationResponse> paramPublicKeyCredential, String paramString, ExternalIdentifier paramExternalIdentifier);
  
  void createBulk(Tenant paramTenant, List<WebAuthnCredential> paramList);
  
  void createWebAuthnCredential(WebAuthnCredential paramWebAuthnCredential);
  
  void delete(WebAuthnCredential paramWebAuthnCredential);
  
  void deleteByUserId(UUID paramUUID);
  
  List<WebAuthnCredential> retrieveAllByUserId(Tenant paramTenant, UUID paramUUID);
  
  WebAuthnCredential retrieveByCredentialId(UUID paramUUID1, UUID paramUUID2, String paramString);
  
  WebAuthnCredential retrieveById(UUID paramUUID1, UUID paramUUID2);
  
  Errors validateBulkImport(Tenant paramTenant, boolean paramBoolean, List<WebAuthnCredential> paramList);
  
  ValidationResult validateCredentialGetDelete(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2);
  
  ValidationResult validateId(Tenant paramTenant, UUID paramUUID);
  
  LoginValidationResult validateLoginComplete(Tenant paramTenant, WebAuthnPublicKeyAuthenticationRequest paramWebAuthnPublicKeyAuthenticationRequest);
  
  StartValidationResult validateLoginStart(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString, List<String> paramList, UUID paramUUID3, WebAuthnWorkflow paramWebAuthnWorkflow);
  
  CompleteValidationResult validateRegistrationComplete(Tenant paramTenant, UUID paramUUID, WebAuthnPublicKeyRegistrationRequest paramWebAuthnPublicKeyRegistrationRequest);
  
  StartValidationResult validateRegistrationStart(Tenant paramTenant, UUID paramUUID, WebAuthnWorkflow paramWebAuthnWorkflow, String paramString);
  
  public static class CompleteValidationResult extends BaseValidationResult {
    public ExternalIdentifier code;
    
    public PublicKeyCredential<AuthenticatorAttestationResponse> credential;
    
    public EventLog partialEventLog;
    
    public String publicKey;
    
    public Tenant tenant;
    
    public User user;
  }
  
  public static class LoginValidationResult extends BaseValidationResult {
    public Application application;
    
    public ExternalIdentifier code;
    
    public EventLog partialEventLog;
    
    public PublicKeyCredential<AuthenticatorAssertionResponse> requestCredential;
    
    public byte[] sigBase;
    
    public Tenant tenant;
    
    public User user;
    
    public WebAuthnCredential webauthnCredential;
  }
  
  public static class StartValidationResult extends BaseValidationResult {
    public Application application;
    
    public List<WebAuthnCredential> credentials;
    
    public Tenant tenant;
    
    public User user;
    
    public UserIdentity userIdentity;
  }
  
  public static class ValidationResult extends BaseValidationResult {
    public WebAuthnCredential existing;
    
    public Tenant tenant;
    
    public User user;
    
    public WebAuthnCredential webauthnCredential;
  }
}
