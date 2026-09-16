package io.fusionauth.api.service.webauthn;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.json.ToString;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.UserMapper;
import io.fusionauth.api.domain.WebAuthnCredentialMapper;
import io.fusionauth.api.domain.webauthn.AttestationFormatIdentifier;
import io.fusionauth.api.domain.webauthn.AuthenticatorAssertionResponse;
import io.fusionauth.api.domain.webauthn.AuthenticatorAttestationResponse;
import io.fusionauth.api.domain.webauthn.AuthenticatorData;
import io.fusionauth.api.domain.webauthn.CoseKey;
import io.fusionauth.api.domain.webauthn.ECCoseKey;
import io.fusionauth.api.domain.webauthn.PublicKeyCredential;
import io.fusionauth.api.domain.webauthn.RSACoseKey;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.IdentityHelper;
import io.fusionauth.api.service.user.IdentityTypeHelper;
import io.fusionauth.api.service.user.IdentityTypeValidator;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.util.EncoderTools;
import io.fusionauth.api.util.HashTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationWebAuthnConfiguration;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.TenantWebAuthnConfiguration;
import io.fusionauth.domain.TenantWebAuthnWorkflowConfiguration;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.WebAuthnCredential;
import io.fusionauth.domain.api.WebAuthnPublicKeyAuthenticationRequest;
import io.fusionauth.domain.api.WebAuthnPublicKeyRegistrationRequest;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.webauthn.AttestationConveyancePreference;
import io.fusionauth.domain.webauthn.AttestationType;
import io.fusionauth.domain.webauthn.AuthenticatorAttachment;
import io.fusionauth.domain.webauthn.AuthenticatorAttachmentPreference;
import io.fusionauth.domain.webauthn.AuthenticatorSelectionCriteria;
import io.fusionauth.domain.webauthn.CoseAlgorithmIdentifier;
import io.fusionauth.domain.webauthn.PublicKeyCredentialCreationOptions;
import io.fusionauth.domain.webauthn.PublicKeyCredentialDescriptor;
import io.fusionauth.domain.webauthn.PublicKeyCredentialParameters;
import io.fusionauth.domain.webauthn.PublicKeyCredentialRelyingPartyEntity;
import io.fusionauth.domain.webauthn.PublicKeyCredentialRequestOptions;
import io.fusionauth.domain.webauthn.PublicKeyCredentialUserEntity;
import io.fusionauth.domain.webauthn.UserVerificationRequirement;
import io.fusionauth.domain.webauthn.WebAuthnRegistrationExtensionOptions;
import io.fusionauth.domain.webauthn.WebAuthnWorkflow;
import io.fusionauth.jwt.InvalidKeyTypeException;
import io.fusionauth.pem.domain.PEM;
import io.fusionauth.webauthn.Verifier;
import io.fusionauth.webauthn.rsa.RSAPSSVerifier;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mybatis.guice.transactional.Transactional;

public class DefaultWebAuthnProviderService implements WebAuthnProviderService {
  private final ApplicationReaderService applicationReaderService;
  
  private final ExternalIdentifierReaderService externalIdentifierReaderService;
  
  private final ExternalIdentifierService externalIdentifierService;
  
  private final ReactorStatusService reactorStatusService;
  
  private final List<CoseAlgorithmIdentifier> signatureAlgorithmPreference = Arrays.asList(new CoseAlgorithmIdentifier[] { CoseAlgorithmIdentifier.ES512, CoseAlgorithmIdentifier.ES384, CoseAlgorithmIdentifier.ES256, CoseAlgorithmIdentifier.PS512, CoseAlgorithmIdentifier.PS384, CoseAlgorithmIdentifier.PS256, CoseAlgorithmIdentifier.RS512, CoseAlgorithmIdentifier.RS384, CoseAlgorithmIdentifier.RS256 });
  
  private final TenantReaderService tenantReaderService;
  
  private final UserMapper userMapper;
  
  private final UserReaderService userReaderService;
  
  private final WebAuthnCredentialMapper webauthnCredentialMapper;
  
  private final WebAuthnService webauthnService;
  
  @Inject
  public DefaultWebAuthnProviderService(ApplicationReaderService paramApplicationReaderService, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, ReactorStatusService paramReactorStatusService, TenantReaderService paramTenantReaderService, UserMapper paramUserMapper, UserReaderService paramUserReaderService, WebAuthnCredentialMapper paramWebAuthnCredentialMapper, WebAuthnService paramWebAuthnService) {
    this.applicationReaderService = paramApplicationReaderService;
    this.externalIdentifierReaderService = paramExternalIdentifierReaderService;
    this.externalIdentifierService = paramExternalIdentifierService;
    this.reactorStatusService = paramReactorStatusService;
    this.tenantReaderService = paramTenantReaderService;
    this.userMapper = paramUserMapper;
    this.userReaderService = paramUserReaderService;
    this.webauthnCredentialMapper = paramWebAuthnCredentialMapper;
    this.webauthnService = paramWebAuthnService;
  }
  
  public PublicKeyCredentialCreationOptions buildCreationOptionsResponse(Tenant paramTenant, User paramUser, WebAuthnWorkflow paramWebAuthnWorkflow, String paramString1, String paramString2, String paramString3) {
    TenantWebAuthnWorkflowConfiguration tenantWebAuthnWorkflowConfiguration = retrieveWebAuthnWorkflowConfiguration(paramTenant.webAuthnConfiguration, null, paramWebAuthnWorkflow);
    List<WebAuthnCredential> list = this.webauthnCredentialMapper.retrieveByUserId(paramTenant.id, paramUser.id);
    String str = (paramString2 != null) ? paramString2 : paramUser.getLogin();
    ExternalIdentifier.ExternalIdData externalIdData = (new ExternalIdentifier.ExternalIdData("workflow", paramWebAuthnWorkflow.toString())).setAttribute("credentialDisplayName", paramString1).setAttribute("credentialName", str).setAttribute("userAgent", paramString3);
    return (new PublicKeyCredentialCreationOptions())
      .with(paramPublicKeyCredentialCreationOptions -> paramPublicKeyCredentialCreationOptions.attestation = AttestationConveyancePreference.none)
      .with(paramPublicKeyCredentialCreationOptions -> paramPublicKeyCredentialCreationOptions.authenticatorSelection = (new AuthenticatorSelectionCriteria()).with(()).with(()))
      
      .with(paramPublicKeyCredentialCreationOptions -> paramPublicKeyCredentialCreationOptions.challenge = this.externalIdentifierService.createWebAuthnRegistrationChallenge(paramTenant, paramUser.id, paramExternalIdData))
      .with(paramPublicKeyCredentialCreationOptions -> paramPublicKeyCredentialCreationOptions.excludeCredentials = (List<PublicKeyCredentialDescriptor>)paramList.stream().map(PublicKeyCredentialDescriptor::new).collect(Collectors.toList()))
      .with(paramPublicKeyCredentialCreationOptions -> paramPublicKeyCredentialCreationOptions.extensions = (new WebAuthnRegistrationExtensionOptions()).with(()))
      .with(paramPublicKeyCredentialCreationOptions -> paramPublicKeyCredentialCreationOptions.pubKeyCredParams = (List<PublicKeyCredentialParameters>)this.signatureAlgorithmPreference.stream().map(PublicKeyCredentialParameters::new).collect(Collectors.toList()))
      .with(paramPublicKeyCredentialCreationOptions -> paramPublicKeyCredentialCreationOptions.relyingParty = (new PublicKeyCredentialRelyingPartyEntity()).with(()).with(()))



      
      .with(paramPublicKeyCredentialCreationOptions -> paramPublicKeyCredentialCreationOptions.user = (new PublicKeyCredentialUserEntity()).with(()).with(()).with(()))

      
      .with(paramPublicKeyCredentialCreationOptions -> paramPublicKeyCredentialCreationOptions.timeout = paramTenant.externalIdentifierConfiguration.webAuthnRegistrationChallengeTimeToLiveInSeconds * 1000L);
  }
  
  public PublicKeyCredentialRequestOptions buildRequestOptionsResponse(Tenant paramTenant, Application paramApplication, User paramUser, UserIdentity paramUserIdentity, List<WebAuthnCredential> paramList, Map<String, Object> paramMap, WebAuthnWorkflow paramWebAuthnWorkflow) {
    TenantWebAuthnWorkflowConfiguration tenantWebAuthnWorkflowConfiguration = retrieveWebAuthnWorkflowConfiguration(paramTenant.webAuthnConfiguration, paramApplication.webAuthnConfiguration, paramWebAuthnWorkflow);
    ExternalIdentifier.ExternalIdData externalIdData = (paramMap == null) ? new ExternalIdentifier.ExternalIdData() : new ExternalIdentifier.ExternalIdData(paramMap);
    externalIdData.setAttribute("workflow", paramWebAuthnWorkflow.toString());
    if (paramUserIdentity != null)
      externalIdData.setAttribute("loginId", paramUserIdentity.value)
        .setAttribute("loginIdType", paramUserIdentity.type); 
    return (new PublicKeyCredentialRequestOptions())
      .with(paramPublicKeyCredentialRequestOptions -> paramPublicKeyCredentialRequestOptions.challenge = this.externalIdentifierService.createWebAuthnAuthenticationChallenge(paramTenant, paramUser.id, paramApplication.id, paramExternalIdData))
      .with(paramPublicKeyCredentialRequestOptions -> paramPublicKeyCredentialRequestOptions.relyingPartyId = paramTenant.webAuthnConfiguration.relyingPartyId)
      .with(paramPublicKeyCredentialRequestOptions -> paramPublicKeyCredentialRequestOptions.allowCredentials = (List<PublicKeyCredentialDescriptor>)paramList.stream().map(PublicKeyCredentialDescriptor::new).collect(Collectors.toList()))
      .with(paramPublicKeyCredentialRequestOptions -> paramPublicKeyCredentialRequestOptions.userVerification = paramTenantWebAuthnWorkflowConfiguration.userVerificationRequirement)
      .with(paramPublicKeyCredentialRequestOptions -> paramPublicKeyCredentialRequestOptions.timeout = paramTenant.externalIdentifierConfiguration.webAuthnAuthenticationChallengeTimeToLiveInSeconds * 1000L);
  }
  
  public WebAuthnCredential completeAssertion(Tenant paramTenant, User paramUser, WebAuthnCredential paramWebAuthnCredential, PublicKeyCredential<AuthenticatorAssertionResponse> paramPublicKeyCredential, byte[] paramArrayOfbyte) {
    this.externalIdentifierService.deleteByUserId(paramUser.id, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.WebAuthnAuthenticationChallenge });
    try {
      buildVerifier(paramWebAuthnCredential.algorithm, paramWebAuthnCredential.publicKey)
        .verify(paramWebAuthnCredential.algorithm, paramArrayOfbyte, Base64.getUrlDecoder().decode(((AuthenticatorAssertionResponse)paramPublicKeyCredential.response).signature));
    } catch (Exception exception) {
      if (paramTenant.webAuthnConfiguration.debug) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Exception: ")
          .append(exception.getClass().getCanonicalName())
          .append("\nMessage: ")
          .append(exception.getMessage());
        if (exception.getCause() != null)
          stringBuilder.append("\nCause: ")
            .append(exception.getCause().getClass().getCanonicalName())
            .append("\nMessage: ")
            .append(exception.getCause().getMessage()); 
        EventLogHelper.create(new EventLog(EventLogType.Debug, "WebAuthn signature verification failed.\nRequest:\n" + 
              
              ToString.toString(paramPublicKeyCredential) + "\nCredential:\n" + 
              
              ToString.toString(paramWebAuthnCredential) + "\nBase64 signature input:\n" + 
              
              EncoderTools.Base64.encodeToString(paramArrayOfbyte) + String.valueOf(stringBuilder)));
      } 
      return null;
    } 
    paramWebAuthnCredential.signCount = ((AuthenticatorAssertionResponse)paramPublicKeyCredential.response).authenticatorData.signCount;
    paramWebAuthnCredential.lastUseInstant = ZonedDateTime.now(ZoneOffset.UTC);
    this.webauthnCredentialMapper.update(paramWebAuthnCredential);
    return paramWebAuthnCredential;
  }
  
  public WebAuthnCredential completeRegistration(Tenant paramTenant, User paramUser, PublicKeyCredential<AuthenticatorAttestationResponse> paramPublicKeyCredential, String paramString, ExternalIdentifier paramExternalIdentifier) {
    this.externalIdentifierService.deleteById(paramExternalIdentifier.id);
    AuthenticatorData authenticatorData = ((AuthenticatorAttestationResponse)paramPublicKeyCredential.response).attestationObject.data;
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    WebAuthnCredential webAuthnCredential = (new WebAuthnCredential()).with(paramWebAuthnCredential -> paramWebAuthnCredential.id = UUID.randomUUID()).with(paramWebAuthnCredential -> paramWebAuthnCredential.credentialId = paramPublicKeyCredential.id).with(paramWebAuthnCredential -> paramWebAuthnCredential.tenantId = paramTenant.id).with(paramWebAuthnCredential -> paramWebAuthnCredential.userId = paramUser.id).with(paramWebAuthnCredential -> paramWebAuthnCredential.algorithm = paramAuthenticatorData.attestedCredentialData.credentialPublicKey.algorithm).with(paramWebAuthnCredential -> paramWebAuthnCredential.attestationType = AttestationType.none).with(paramWebAuthnCredential -> paramWebAuthnCredential.authenticatorSupportsUserVerification = paramAuthenticatorData.userVerified()).with(paramWebAuthnCredential -> paramWebAuthnCredential.discoverable = paramPublicKeyCredential.clientExtensionResults.isDiscoverableCredential()).with(paramWebAuthnCredential -> paramWebAuthnCredential.transports = paramPublicKeyCredential.transports).with(paramWebAuthnCredential -> paramWebAuthnCredential.displayName = paramExternalIdentifier.getAttribute("credentialDisplayName")).with(paramWebAuthnCredential -> paramWebAuthnCredential.name = paramExternalIdentifier.getAttribute("credentialName")).with(paramWebAuthnCredential -> paramWebAuthnCredential.userAgent = paramExternalIdentifier.getAttribute("userAgent")).with(paramWebAuthnCredential -> paramWebAuthnCredential.publicKey = paramString).with(paramWebAuthnCredential -> paramWebAuthnCredential.relyingPartyId = resolveRelyingPartyId(paramTenant.webAuthnConfiguration.relyingPartyId, ((AuthenticatorAttestationResponse)paramPublicKeyCredential.response).clientData.origin)).with(paramWebAuthnCredential -> paramWebAuthnCredential.signCount = paramAuthenticatorData.signCount).with(paramWebAuthnCredential -> paramWebAuthnCredential.insertInstant = paramZonedDateTime).with(paramWebAuthnCredential -> paramWebAuthnCredential.lastUseInstant = paramZonedDateTime);
    this.webauthnCredentialMapper.create(webAuthnCredential);
    return webAuthnCredential;
  }
  
  @Transactional
  public void createBulk(Tenant paramTenant, List<WebAuthnCredential> paramList) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    for (byte b = 0; b < paramList.size(); b += 3000) {
      int i = Math.min(b + 3000, paramList.size());
      List<WebAuthnCredential> list = paramList.subList(b, i);
      list.stream().filter(paramWebAuthnCredential -> (paramWebAuthnCredential.id == null))
        .forEach(paramWebAuthnCredential -> paramWebAuthnCredential.id = UUID.randomUUID());
      list.forEach(paramWebAuthnCredential -> paramWebAuthnCredential.tenantId = paramTenant.id);
      list.stream().filter(paramWebAuthnCredential -> (paramWebAuthnCredential.attestationType == null))
        .forEach(paramWebAuthnCredential -> paramWebAuthnCredential.attestationType = AttestationType.none);
      list.stream().filter(paramWebAuthnCredential -> (paramWebAuthnCredential.insertInstant == null))
        .forEach(paramWebAuthnCredential -> paramWebAuthnCredential.insertInstant = paramZonedDateTime);
      list.stream().filter(paramWebAuthnCredential -> (paramWebAuthnCredential.lastUseInstant == null))
        .forEach(paramWebAuthnCredential -> paramWebAuthnCredential.lastUseInstant = paramZonedDateTime);
      this.webauthnCredentialMapper.createBulk(list);
    } 
  }
  
  public void createWebAuthnCredential(WebAuthnCredential paramWebAuthnCredential) {
    this.webauthnCredentialMapper.create(paramWebAuthnCredential);
  }
  
  public void delete(WebAuthnCredential paramWebAuthnCredential) {
    this.webauthnCredentialMapper.delete(paramWebAuthnCredential.id);
  }
  
  public void deleteByUserId(UUID paramUUID) {
    this.webauthnCredentialMapper.deleteByUserId(paramUUID);
  }
  
  public List<WebAuthnCredential> retrieveAllByUserId(Tenant paramTenant, UUID paramUUID) {
    return this.webauthnCredentialMapper.retrieveByUserId((paramTenant != null) ? paramTenant.id : null, paramUUID);
  }
  
  public WebAuthnCredential retrieveByCredentialId(UUID paramUUID1, UUID paramUUID2, String paramString) {
    return this.webauthnCredentialMapper.retrieveByCredentialId(paramUUID1, paramUUID2, paramString);
  }
  
  public WebAuthnCredential retrieveById(UUID paramUUID1, UUID paramUUID2) {
    return this.webauthnCredentialMapper.retrieveById(paramUUID1, paramUUID2);
  }
  
  public Errors validateBulkImport(Tenant paramTenant, boolean paramBoolean, List<WebAuthnCredential> paramList) {
    int i = 1;
    int j = 1;
    int k = 1;
    int m = 1;
    int n = 1;
    int i1 = 1;
    TreeSet<String> treeSet1 = new TreeSet();
    TreeSet<String> treeSet2 = new TreeSet();
    for (WebAuthnCredential webAuthnCredential : paramList) {
      i &= (webAuthnCredential.userId != null) ? 1 : 0;
      j &= !StringTools.isTrimmedEmpty(webAuthnCredential.displayName) ? 1 : 0;
      k &= !StringTools.isTrimmedEmpty(webAuthnCredential.name) ? 1 : 0;
      m &= (webAuthnCredential.algorithm != null) ? 1 : 0;
      if (webAuthnCredential.algorithm != null && !this.signatureAlgorithmPreference.contains(webAuthnCredential.algorithm))
        treeSet1.add(webAuthnCredential.algorithm.toString()); 
      n &= !StringTools.isTrimmedEmpty(webAuthnCredential.publicKey) ? 1 : 0;
      if (webAuthnCredential.algorithm != null && !StringTools.isTrimmedEmpty(webAuthnCredential.publicKey)) {
        Verifier verifier = null;
        try {
          verifier = buildVerifier(webAuthnCredential.algorithm, webAuthnCredential.publicKey);
        } catch (Exception exception) {}
        if (verifier == null)
          treeSet2.add(webAuthnCredential.publicKey); 
      } 
      i1 &= !StringTools.isTrimmedEmpty(webAuthnCredential.credentialId) ? 1 : 0;
    } 
    return (new Validator())
      .ensure(i, "credential.userId", "[missing]", new Object[0])
      .ensure(j, "credential.displayName", "[blank]", new Object[0])
      .ensure(k, "credential.name", "[blank]", new Object[0])
      .ensure(m, "credential.algorithm", "[missing]", new Object[0])
      .ensure(treeSet1.isEmpty(), "credential.algorithm", "[invalid]", new Object[] { String.join(", ", (Iterable)treeSet1) }).ensure(n, "credential.publicKey", "[blank]", new Object[0])
      .valid(treeSet2.isEmpty(), "credential.publicKey", new Object[] { String.join(", ", (Iterable)treeSet2) }).ensure(i1, "credential.credentialId", "[blank]", new Object[0])
      .ifTrue(paramBoolean, paramValidator -> paramValidator.ifNoErrors(()))

      
      .done();
  }
  
  public WebAuthnProviderService.ValidationResult validateCredentialGetDelete(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2) {
    WebAuthnProviderService.ValidationResult validationResult = new WebAuthnProviderService.ValidationResult();
    if (paramUUID1 == null && paramUUID2 == null) {
      validationResult.errors.addGeneralError("[invalid]", null, new Object[0]);
    } else if (paramUUID1 != null) {
      validationResult.existing = this.webauthnCredentialMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID1);
    } else {
      validationResult.user = this.userReaderService.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID2);
      validationResult
        
        .errors = (new Validator()).validObject(validationResult.user, "userId", new Object[] { paramUUID2 }).done();
    } 
    validationResult.tenant = this.tenantReaderService.resolve(paramTenant, new Tenantable[] { validationResult.existing, validationResult.user });
    return validationResult;
  }
  
  public WebAuthnProviderService.ValidationResult validateId(Tenant paramTenant, UUID paramUUID) {
    WebAuthnProviderService.ValidationResult validationResult = new WebAuthnProviderService.ValidationResult();
    validationResult.existing = (paramUUID != null) ? this.webauthnCredentialMapper.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID) : null;
    validationResult.tenant = this.tenantReaderService.resolve(paramTenant, new Tenantable[] { validationResult.existing });
    validationResult.errors = (new Validator()).notMissing(paramUUID, "id", new Object[0]).done();
    return validationResult;
  }
  
  public WebAuthnProviderService.LoginValidationResult validateLoginComplete(Tenant paramTenant, WebAuthnPublicKeyAuthenticationRequest paramWebAuthnPublicKeyAuthenticationRequest) {
    Validator validator = new Validator();
    WebAuthnProviderService.LoginValidationResult loginValidationResult = new WebAuthnProviderService.LoginValidationResult();
    loginValidationResult.requestCredential = this.webauthnService.parseWebAuthnAuthenticationRequest(paramWebAuthnPublicKeyAuthenticationRequest, validator);
    if (validator.hasErrors()) {
      loginValidationResult.errors.add(validator.done());
      if (paramTenant != null && paramTenant.webAuthnConfiguration.debug)
        loginValidationResult
          
          .partialEventLog = new EventLog(EventLogType.Debug, "Parsing WebAuthn authentication request failed.\nEncoded request:\n" + ToString.toString(paramWebAuthnPublicKeyAuthenticationRequest) + "\nErrors:\n"); 
      return loginValidationResult;
    } 
    ExternalIdentifierReaderService.ValidationResult validationResult = this.externalIdentifierReaderService.validate(paramTenant, ((AuthenticatorAssertionResponse)loginValidationResult.requestCredential.response).clientData.challenge, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.WebAuthnAuthenticationChallenge });
    loginValidationResult.code = validationResult.id;
    loginValidationResult.tenant = this.tenantReaderService.resolve(paramTenant, new Tenantable[] { loginValidationResult.code });
    if (loginValidationResult.tenant == null || loginValidationResult.code == null)
      return loginValidationResult; 
    loginValidationResult.application = this.applicationReaderService.retrieveById(loginValidationResult.tenant.id, loginValidationResult.code.applicationId);
    loginValidationResult.user = this.userReaderService.retrieveById(loginValidationResult.tenant.id, loginValidationResult.code.userId);
    WebAuthnWorkflow webAuthnWorkflow = WebAuthnWorkflow.valueOf(loginValidationResult.code.data.getAttribute("workflow"));
    TenantWebAuthnWorkflowConfiguration tenantWebAuthnWorkflowConfiguration = retrieveWebAuthnWorkflowConfiguration(loginValidationResult.tenant.webAuthnConfiguration, loginValidationResult.application.webAuthnConfiguration, webAuthnWorkflow);
    validator.ensure(loginValidationResult.tenant.webAuthnConfiguration.enabled, "workflow", "[disabled]", new Object[] { webAuthnWorkflow }).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure((paramWebAuthnWorkflow == WebAuthnWorkflow.bootstrap || paramWebAuthnWorkflow == WebAuthnWorkflow.reauthentication), "workflow", "[invalid]", new Object[] { paramWebAuthnWorkflow }).ifLastCheckHadNoError(()));
    if (validator.hasErrors()) {
      loginValidationResult.errors.add(validator.done());
      if (loginValidationResult.tenant.webAuthnConfiguration.debug)
        loginValidationResult.partialEventLog = new EventLog(EventLogType.Debug, "WebAuthn is disabled during Complete Authentication.\nErrors:\n"); 
      return loginValidationResult;
    } 
    loginValidationResult.webauthnCredential = this.webauthnCredentialMapper.retrieveByCredentialId(loginValidationResult.tenant.id, loginValidationResult.user.id, loginValidationResult.requestCredential.id);
    String str = resolveRelyingPartyId(loginValidationResult.tenant.webAuthnConfiguration.relyingPartyId, ((AuthenticatorAssertionResponse)loginValidationResult.requestCredential.response).clientData.origin);
    validator
      
      .validObject(loginValidationResult.webauthnCredential, "credential.id", new Object[] { "The credential does not exist or belongs to another user." }).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(this.signatureAlgorithmPreference.contains(paramLoginValidationResult.webauthnCredential.algorithm), "credential.id", "[invalid]", new Object[] { String.format("The signing algorithm value of [%s] for the included credential is not valid. The supported values are [%s].", new Object[] { Long.valueOf(paramLoginValidationResult.webauthnCredential.algorithm.registryId), this.signatureAlgorithmPreference.stream().map(()).toList() }) }).ifTrue((((AuthenticatorAssertionResponse)paramLoginValidationResult.requestCredential.response).authenticatorData.signCount > 0 || paramLoginValidationResult.webauthnCredential.signCount > 0), ()))
















      
      .ensure((
        StringTools.isBlank(((AuthenticatorAssertionResponse)loginValidationResult.requestCredential.response).userHandle) || loginValidationResult.user.id.toString().equals(new String(Base64.getUrlDecoder().decode(((AuthenticatorAssertionResponse)loginValidationResult.requestCredential.response).userHandle), StandardCharsets.UTF_8))), "credential.response.userHandle", "[invalid]", new Object[0])



      
      .ensure(((AuthenticatorAssertionResponse)loginValidationResult.requestCredential.response).clientData.type.equals("webauthn.get"), "credential.response.clientDataJSON", "[invalid]", new Object[] { "This operation must use the [webauthn.get] type." }).ensure(((AuthenticatorAssertionResponse)loginValidationResult.requestCredential.response).clientData.challenge.equals(loginValidationResult.code.id), "credential.response.clientDataJSON", "[invalid]", new Object[] { "The challenge must match the value from the start of the ceremony." }).ensure(isOriginValidForRpId(((AuthenticatorAssertionResponse)loginValidationResult.requestCredential.response).clientData.origin, str), "origin", "[invalid]", new Object[] { ((AuthenticatorAssertionResponse)loginValidationResult.requestCredential.response).clientData.origin, str }).ensure((str != null && 
        
        EncoderTools.Base64.encodeToString(((AuthenticatorAssertionResponse)loginValidationResult.requestCredential.response).authenticatorData.rpIdHash).equals(HashTools.sha256(str))), "rpId", "[invalid]", new Object[0])



      
      .ensure(((AuthenticatorAssertionResponse)loginValidationResult.requestCredential.response).authenticatorData.userPresent(), "credential.response.authenticatorData", "[invalid]", new Object[] { "User presence is required during a WebAuthn ceremony." }).ifTrue((tenantWebAuthnWorkflowConfiguration.userVerificationRequirement == UserVerificationRequirement.required), paramValidator -> paramValidator.ensure(((AuthenticatorAssertionResponse)paramLoginValidationResult.requestCredential.response).authenticatorData.userVerified(), "credential.response.authenticatorData", "[invalid]", new Object[] { "User verification was requested for the operation but not provided." }));
    byte[] arrayOfByte = EncoderTools.Base64.decode(HashTools.sha256(new String(Base64.getUrlDecoder().decode(paramWebAuthnPublicKeyAuthenticationRequest.response.clientDataJSON), StandardCharsets.UTF_8)));
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byteArrayOutputStream.write(Base64.getUrlDecoder().decode(paramWebAuthnPublicKeyAuthenticationRequest.response.authenticatorData));
      byteArrayOutputStream.write(arrayOfByte);
      loginValidationResult.sigBase = byteArrayOutputStream.toByteArray();
    } catch (IOException iOException) {}
    loginValidationResult.errors.add(validator.done());
    if (!loginValidationResult.errors.empty() && loginValidationResult.tenant.webAuthnConfiguration.debug)
      loginValidationResult
        
        .partialEventLog = new EventLog(EventLogType.Debug, "WebAuthn Authentication validation failed.\nCredential:\n" + ToString.toString(loginValidationResult.webauthnCredential) + "\nErrors:\n"); 
    return loginValidationResult;
  }
  
  public WebAuthnProviderService.StartValidationResult validateLoginStart(Tenant paramTenant, UUID paramUUID1, UUID paramUUID2, String paramString, List<String> paramList, UUID paramUUID3, WebAuthnWorkflow paramWebAuthnWorkflow) {
    WebAuthnProviderService.StartValidationResult startValidationResult = new WebAuthnProviderService.StartValidationResult();
    startValidationResult.application = this.applicationReaderService.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID1);
    startValidationResult.tenant = this.tenantReaderService.resolve(paramTenant, new Tenantable[] { startValidationResult.application });
    if (startValidationResult.tenant == null)
      return startValidationResult; 
    startValidationResult.errors.add((new Validator())
        
        .ifTrue(StringTools.isTrimmedEmpty(paramString), paramValidator -> paramValidator.notMissing(paramUUID, "userId", new Object[0]))
        
        .ifLastCheckHadNoError(paramValidator -> IdentityTypeValidator.validate(paramValidator, paramList, "loginIdTypes"))
        .ifNoErrors(() -> {
            List<IdentityType> list = IdentityTypeHelper.convert(paramList);
            paramStartValidationResult.user = (paramUUID1 != null) ? this.userReaderService.retrieveById((paramStartValidationResult.tenant != null) ? paramStartValidationResult.tenant.id : null, paramUUID1) : this.userReaderService.retrieveByLoginId((paramStartValidationResult.tenant != null) ? paramStartValidationResult.tenant.id : null, paramString, list);
            if (paramUUID1 == null && paramStartValidationResult.user != null)
              paramStartValidationResult.userIdentity = IdentityHelper.resolveIdentity(paramStartValidationResult.user, paramString, list); 
            paramStartValidationResult.credentials = (paramUUID2 != null) ? List.of(this.webauthnCredentialMapper.retrieveById(paramStartValidationResult.tenant.id, paramUUID2)) : ((paramStartValidationResult.user != null) ? this.webauthnCredentialMapper.retrieveByUserId(paramStartValidationResult.tenant.id, paramStartValidationResult.user.id) : null);
          }).notMissing(paramUUID1, "applicationId", new Object[0])
        .ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramStartValidationResult.application, "applicationId", new Object[] { paramUUID })).ifTrue((paramUUID3 != null), paramValidator -> paramValidator.ensure((paramStartValidationResult.credentials.size() == 1), "credentialId", "[invalid]", new Object[] { paramUUID })).ifTrue((paramUUID3 == null && startValidationResult.user != null), paramValidator -> paramValidator.ifTrue((paramUUID != null), ()).ifTrue((paramUUID == null), ()))






        
        .notBlank(paramWebAuthnWorkflow, "workflow", new Object[0])
        .ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(paramStartValidationResult.tenant.webAuthnConfiguration.enabled, "workflow", "[disabled]", new Object[] { paramWebAuthnWorkflow }).ifLastCheckHadNoError(())).done());
    return startValidationResult;
  }
  
  public WebAuthnProviderService.CompleteValidationResult validateRegistrationComplete(Tenant paramTenant, UUID paramUUID, WebAuthnPublicKeyRegistrationRequest paramWebAuthnPublicKeyRegistrationRequest) {
    Validator validator = new Validator();
    WebAuthnProviderService.CompleteValidationResult completeValidationResult = new WebAuthnProviderService.CompleteValidationResult();
    completeValidationResult.user = this.userReaderService.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID);
    completeValidationResult.tenant = this.tenantReaderService.resolve(paramTenant, new Tenantable[] { completeValidationResult.user });
    completeValidationResult.credential = this.webauthnService.parseWebAuthnRegistrationRequest(paramWebAuthnPublicKeyRegistrationRequest, validator);
    if (validator.hasErrors()) {
      completeValidationResult.errors.add(validator.done());
      if (completeValidationResult.tenant != null && completeValidationResult.tenant.webAuthnConfiguration.debug)
        completeValidationResult
          
          .partialEventLog = new EventLog(EventLogType.Debug, "Parsing WebAuthn registration request failed.\nEncoded request:\n" + ToString.toString(paramWebAuthnPublicKeyRegistrationRequest) + "\nErrors:\n"); 
      return completeValidationResult;
    } 
    ExternalIdentifierReaderService.ValidationResult validationResult = this.externalIdentifierReaderService.validate(completeValidationResult.tenant, ((AuthenticatorAttestationResponse)completeValidationResult.credential.response).clientData.challenge, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.WebAuthnRegistrationChallenge });
    completeValidationResult.code = validationResult.id;
    if (completeValidationResult.tenant == null || completeValidationResult.user == null || completeValidationResult.code == null)
      return completeValidationResult; 
    WebAuthnWorkflow webAuthnWorkflow = WebAuthnWorkflow.valueOf(completeValidationResult.code.data.getAttribute("workflow"));
    TenantWebAuthnWorkflowConfiguration tenantWebAuthnWorkflowConfiguration = retrieveWebAuthnWorkflowConfiguration(completeValidationResult.tenant.webAuthnConfiguration, null, webAuthnWorkflow);
    validator.ensure(completeValidationResult.tenant.webAuthnConfiguration.enabled, "workflow", "[disabled]", new Object[] { webAuthnWorkflow }).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(paramTenantWebAuthnWorkflowConfiguration.enabled, "workflow", "[disabled]", new Object[] { paramWebAuthnWorkflow }));
    if (validator.hasErrors()) {
      completeValidationResult.errors.add(validator.done());
      if (completeValidationResult.tenant.webAuthnConfiguration.debug)
        completeValidationResult.partialEventLog = new EventLog(EventLogType.Debug, "WebAuthn is disabled during Complete Registration.\nErrors:\n"); 
      return completeValidationResult;
    } 
    String str = resolveRelyingPartyId(completeValidationResult.tenant.webAuthnConfiguration.relyingPartyId, ((AuthenticatorAttestationResponse)completeValidationResult.credential.response).clientData.origin);
    validator
      
      .ensure(((AuthenticatorAttestationResponse)completeValidationResult.credential.response).clientData.type.equals("webauthn.create"), "credential.response.clientDataJSON", "[invalid]", new Object[] { "The [clientData.type] is invalid. Expected [webauthn.create] but found [" + ((AuthenticatorAttestationResponse)completeValidationResult.credential.response).clientData.type + "]." }).ensure(((AuthenticatorAttestationResponse)completeValidationResult.credential.response).clientData.challenge.equals(completeValidationResult.code.id), "credential.response.clientDataJSON", "[invalid]", new Object[] { "The [clientData.challenge] is invalid. Expected [" + ((AuthenticatorAttestationResponse)completeValidationResult.credential.response).clientData.challenge + "] but found [" + completeValidationResult.code.id + "]. The challenge must match the value from the start of the ceremony." }).ensure(isOriginValidForRpId(((AuthenticatorAttestationResponse)completeValidationResult.credential.response).clientData.origin, str), "origin", "[invalid]", new Object[] { ((AuthenticatorAttestationResponse)completeValidationResult.credential.response).clientData.origin, str }).ensure((str != null && 
        
        EncoderTools.Base64.encodeToString(((AuthenticatorAttestationResponse)completeValidationResult.credential.response).attestationObject.data.rpIdHash).equals(HashTools.sha256(str))), "rpId", "[invalid]", new Object[0])



      
      .ensure(((AuthenticatorAttestationResponse)completeValidationResult.credential.response).attestationObject.data.userPresent(), "credential.response.attestationObject", "[invalid]", new Object[] { "User presence is required during a WebAuthn ceremony." }).ifTrue((tenantWebAuthnWorkflowConfiguration.userVerificationRequirement == UserVerificationRequirement.required), paramValidator -> paramValidator.ensure(((AuthenticatorAttestationResponse)paramCompleteValidationResult.credential.response).attestationObject.data.userVerified(), "credential.response.attestationObject", "[invalid]", new Object[] { "User verification was requested for the operation but not provided." })).ensure((((AuthenticatorAttestationResponse)completeValidationResult.credential.response).attestationObject.data.attestedCredentialData.credentialPublicKey != null), "credential.response.attestationObject", "[invalid]", new Object[] { "The request does not contain a public key." }).ifLastCheckHadNoError(paramValidator -> paramValidator.ensure((((AuthenticatorAttestationResponse)paramCompleteValidationResult.credential.response).attestationObject.data.attestedCredentialData.credentialPublicKey.algorithm != null), "credential.response.attestationObject", "[invalid]", new Object[] { String.format("The signing algorithm value is missing for the included credential is required. The supported values are [%s].", new Object[] { this.signatureAlgorithmPreference.stream().map(()).toList() }) }).ifLastCheckHadNoError(())).ensure((((AuthenticatorAttestationResponse)completeValidationResult.credential.response).attestationObject.formatId == AttestationFormatIdentifier.none), "credential.response.attestationObject", "[invalid]", new Object[] { String.format("The attestation format Id is not valid. The supported values are %s.", new Object[] { Collections.singletonList(AttestationFormatIdentifier.none) }) });
    try {
      completeValidationResult.publicKey = extractPublicKeyPem(((AuthenticatorAttestationResponse)completeValidationResult.credential.response).attestationObject.data.attestedCredentialData.credentialPublicKey);
    } catch (NoSuchAlgorithmException|InvalidParameterSpecException|InvalidKeySpecException|InvalidKeyTypeException noSuchAlgorithmException) {
      completeValidationResult.errors.addFieldError("credential.response.attestationObject", "[invalid]credential.response.attestationObject", 
          String.format("There was an error parsing the public key on the request: %s", new Object[] { noSuchAlgorithmException.getMessage() }), new Object[0]);
    } 
    completeValidationResult.errors.add(validator.done());
    if (!completeValidationResult.errors.empty() && completeValidationResult.tenant.webAuthnConfiguration.debug)
      completeValidationResult
        
        .partialEventLog = new EventLog(EventLogType.Debug, "WebAuthn Registration validation failed.\nCredential:\n" + ToString.toString(completeValidationResult.credential) + "\nErrors:\n"); 
    return completeValidationResult;
  }
  
  public WebAuthnProviderService.StartValidationResult validateRegistrationStart(Tenant paramTenant, UUID paramUUID, WebAuthnWorkflow paramWebAuthnWorkflow, String paramString) {
    WebAuthnProviderService.StartValidationResult startValidationResult = new WebAuthnProviderService.StartValidationResult();
    startValidationResult.user = this.userReaderService.retrieveById((paramTenant != null) ? paramTenant.id : null, paramUUID);
    startValidationResult.tenant = this.tenantReaderService.resolve(paramTenant, new Tenantable[] { startValidationResult.user });
    if (startValidationResult.tenant == null)
      return startValidationResult; 
    startValidationResult.errors.add((new Validator())
        
        .notBlank(paramString, "displayName", new Object[0])
        .notMissing(paramUUID, "userId", new Object[0])
        
        .notBlank(paramWebAuthnWorkflow, "workflow", new Object[0])
        .ifLastCheckHadNoError(paramValidator -> paramValidator.ensure(paramStartValidationResult.tenant.webAuthnConfiguration.enabled, "workflow", "[disabled]", new Object[] { paramWebAuthnWorkflow }).ifLastCheckHadNoError(())).done());
    return startValidationResult;
  }
  
  private Verifier buildVerifier(CoseAlgorithmIdentifier paramCoseAlgorithmIdentifier, String paramString) {
    switch (paramCoseAlgorithmIdentifier) {
      default:
        throw new MatchException(null, null);
      case bootstrap:
      case general:
      case reauthentication:
      
      case null:
      case null:
      case null:
      
      case null:
      case null:
      case null:
        break;
    } 
    return 

      
      RSAPSSVerifier.newVerifier(paramString);
  }
  
  private String effectiveDomain(String paramString) {
    if (paramString == null)
      return null; 
    try {
      URI uRI = URI.create(paramString);
      String str = uRI.getHost();
      boolean bool = ("http".equalsIgnoreCase(uRI.getScheme()) && ("localhost".equalsIgnoreCase(str) || "127.0.0.1".equals(str) || "[::1]".equals(str))) ? true : false;
      if (!"https".equalsIgnoreCase(uRI.getScheme()) && !bool)
        return null; 
      return str;
    } catch (IllegalArgumentException illegalArgumentException) {
      return null;
    } 
  }
  
  private String extractPublicKeyPem(CoseKey paramCoseKey) throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException, InvalidKeyTypeException {
    if (paramCoseKey instanceof ECCoseKey) {
      ECCoseKey eCCoseKey = (ECCoseKey)paramCoseKey;
      AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance("EC");
      algorithmParameters.init(new ECGenParameterSpec(eCCoseKey.curveId.curve));
      ECParameterSpec eCParameterSpec = algorithmParameters.<ECParameterSpec>getParameterSpec(ECParameterSpec.class);
      ECPoint eCPoint = new ECPoint(new BigInteger(eCCoseKey.x), new BigInteger(eCCoseKey.y));
      ECPublicKeySpec eCPublicKeySpec = new ECPublicKeySpec(eCPoint, eCParameterSpec);
      KeyFactory keyFactory = KeyFactory.getInstance("EC");
      PublicKey publicKey = keyFactory.generatePublic(eCPublicKeySpec);
      return PEM.encode(publicKey);
    } 
    if (paramCoseKey instanceof RSACoseKey) {
      RSACoseKey rSACoseKey = (RSACoseKey)paramCoseKey;
      PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(new BigInteger(1, rSACoseKey.n), new BigInteger(1, rSACoseKey.e)));
      return PEM.encode(publicKey);
    } 
    throw new InvalidKeyTypeException("The key type " + String.valueOf(paramCoseKey.keyType()) + "(" + (paramCoseKey.keyType()).registryId + ") is invalid");
  }
  
  private AuthenticatorAttachment getAuthenticatorAttachment(AuthenticatorAttachmentPreference paramAuthenticatorAttachmentPreference) {
    switch (paramAuthenticatorAttachmentPreference) {
      case bootstrap:
      
      case general:
      
    } 
    return 



      
      null;
  }
  
  private boolean isOriginValidForRpId(String paramString1, String paramString2) {
    if (paramString2 == null)
      return false; 
    String str = effectiveDomain(paramString1);
    if (str == null)
      return false; 
    return (str.equals(paramString2) || str.endsWith("." + paramString2));
  }
  
  private String resolveRelyingPartyId(String paramString1, String paramString2) {
    return !StringTools.isBlank(paramString1) ? paramString1 : effectiveDomain(paramString2);
  }
  
  private TenantWebAuthnWorkflowConfiguration retrieveWebAuthnWorkflowConfiguration(TenantWebAuthnConfiguration paramTenantWebAuthnConfiguration, ApplicationWebAuthnConfiguration paramApplicationWebAuthnConfiguration, WebAuthnWorkflow paramWebAuthnWorkflow) {
    switch (paramWebAuthnWorkflow) {
      default:
        throw new MatchException(null, null);
      case bootstrap:
      
      case general:
      
      case reauthentication:
        break;
    } 
    TenantWebAuthnWorkflowConfiguration tenantWebAuthnWorkflowConfiguration = 








      
      new TenantWebAuthnWorkflowConfiguration(paramTenantWebAuthnConfiguration.reauthenticationWorkflow);
    if (paramApplicationWebAuthnConfiguration != null && paramApplicationWebAuthnConfiguration.enabled)
      if (paramWebAuthnWorkflow == WebAuthnWorkflow.bootstrap) {
        tenantWebAuthnWorkflowConfiguration.enabled = paramApplicationWebAuthnConfiguration.bootstrapWorkflow.enabled;
      } else if (paramWebAuthnWorkflow == WebAuthnWorkflow.reauthentication) {
        tenantWebAuthnWorkflowConfiguration.enabled = paramApplicationWebAuthnConfiguration.reauthenticationWorkflow.enabled;
      }  
    return tenantWebAuthnWorkflowConfiguration;
  }
  
  private void validateBulkImportDbConstraints(Validator paramValidator, UUID paramUUID, List<WebAuthnCredential> paramList) {
    TreeSet<String> treeSet1 = new TreeSet();
    List list = paramList.stream().filter(paramWebAuthnCredential -> (paramWebAuthnCredential.userId != null)).map(paramWebAuthnCredential -> paramWebAuthnCredential.userId).distinct().toList();
    for (UUID uUID : list) {
      if (this.userMapper.existsById(paramUUID, uUID) == null)
        treeSet1.add(uUID.toString()); 
    } 
    paramValidator.valid(treeSet1.isEmpty(), "credential.userId", new Object[] { String.join(", ", (Iterable)treeSet1) });
    TreeSet<String> treeSet2 = new TreeSet();
    TreeSet<UUID> treeSet = new TreeSet();
    for (WebAuthnCredential webAuthnCredential : paramList) {
      if (webAuthnCredential.id == null)
        continue; 
      if (treeSet.contains(webAuthnCredential.id) || this.webauthnCredentialMapper.existsById(paramUUID, webAuthnCredential.id) != null) {
        treeSet2.add(webAuthnCredential.id.toString());
        continue;
      } 
      treeSet.add(webAuthnCredential.id);
    } 
    paramValidator.ensure(treeSet2.isEmpty(), "credential.id", "[duplicate]", new Object[] { String.join(", ", (Iterable)treeSet2) });
  }
}
