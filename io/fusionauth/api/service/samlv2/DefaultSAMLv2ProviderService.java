package io.fusionauth.api.service.samlv2;

import com.google.inject.Inject;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.domain.api.service.MutableLambdaArgument;
import io.fusionauth.api.domain.guice.FusionAuthTenantIdProvider;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.system.ApplicationReaderService;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.util.XMLTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdPInitiatedIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import io.fusionauth.pem.domain.PEM;
import io.fusionauth.samlv2.domain.Algorithm;
import io.fusionauth.samlv2.domain.Assertion;
import io.fusionauth.samlv2.domain.AuthenticationRequest;
import io.fusionauth.samlv2.domain.Binding;
import io.fusionauth.samlv2.domain.Conditions;
import io.fusionauth.samlv2.domain.ConfirmationMethod;
import io.fusionauth.samlv2.domain.DigestAlgorithm;
import io.fusionauth.samlv2.domain.EncryptionAlgorithm;
import io.fusionauth.samlv2.domain.KeyLocation;
import io.fusionauth.samlv2.domain.KeyTransportAlgorithm;
import io.fusionauth.samlv2.domain.LogoutRequest;
import io.fusionauth.samlv2.domain.MaskGenerationFunction;
import io.fusionauth.samlv2.domain.MetaData;
import io.fusionauth.samlv2.domain.NameID;
import io.fusionauth.samlv2.domain.NameIDFormat;
import io.fusionauth.samlv2.domain.ResponseStatus;
import io.fusionauth.samlv2.domain.SAMLException;
import io.fusionauth.samlv2.domain.SAMLRequest;
import io.fusionauth.samlv2.domain.SAMLResponse;
import io.fusionauth.samlv2.domain.SignatureLocation;
import io.fusionauth.samlv2.domain.StandardClaim;
import io.fusionauth.samlv2.domain.Subject;
import io.fusionauth.samlv2.domain.SubjectConfirmation;
import io.fusionauth.samlv2.service.SAMLv2Service;
import io.fusionauth.samlv2.util.SAMLTools;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primeframework.mvc.ErrorException;

public class DefaultSAMLv2ProviderService implements SAMLv2ProviderService {
  private static final List<String> SupportedNameIdFormats = List.of(NameIDFormat.EmailAddress.toSAMLFormat(), NameIDFormat.Persistent
      .toSAMLFormat(), NameIDFormat.Unspecified
      .toSAMLFormat());
  
  private final ApplicationCache applicationCache;
  
  private final ApplicationReaderService applicationReader;
  
  private final FusionAuthTenantIdProvider fusionAuthTenantIdProvider;
  
  private final IdentityProviderCache identityProviderCache;
  
  private final KeyCache keyCache;
  
  private final KeyReaderService keyReader;
  
  private final LambdaInvocationService lambdaInvocationService;
  
  private final SAMLv2Service samlv2Service;
  
  @Inject
  public DefaultSAMLv2ProviderService(ApplicationCache paramApplicationCache, ApplicationReaderService paramApplicationReaderService, FusionAuthTenantIdProvider paramFusionAuthTenantIdProvider, IdentityProviderCache paramIdentityProviderCache, KeyCache paramKeyCache, KeyReaderService paramKeyReaderService, LambdaInvocationService paramLambdaInvocationService, SAMLv2Service paramSAMLv2Service) {
    this.applicationCache = paramApplicationCache;
    this.applicationReader = paramApplicationReaderService;
    this.identityProviderCache = paramIdentityProviderCache;
    this.fusionAuthTenantIdProvider = paramFusionAuthTenantIdProvider;
    this.keyCache = paramKeyCache;
    this.keyReader = paramKeyReaderService;
    this.lambdaInvocationService = paramLambdaInvocationService;
    this.samlv2Service = paramSAMLv2Service;
  }
  
  public String buildIdPMetaDataResponse(UUID paramUUID, String paramString) throws SAMLException {
    if (paramUUID == null)
      paramUUID = this.fusionAuthTenantIdProvider.get(); 
    MetaData metaData = new MetaData();
    metaData.id = paramUUID.toString();
    metaData.entityId = SAMLv2Helper.getIdentityProviderEntityId(paramString, paramUUID);
    metaData.idp = new MetaData.IDPMetaData();
    metaData.idp.postBindingSignInEndpoints.add(paramString + "/samlv2/login/" + paramString);
    metaData.idp.redirectBindingSignInEndpoints.add(metaData.idp.postBindingSignInEndpoints.get(0));
    metaData.idp.postBindingLogoutEndpoints.add(paramString + "/samlv2/logout/" + paramString);
    metaData.idp.redirectBindingLogoutEndpoints.add(metaData.idp.postBindingLogoutEndpoints.get(0));
    metaData.idp.wantAuthnRequestsSigned = true;
    List<Application> list = this.applicationCache.getAllByTenantId(paramUUID);
    Objects.requireNonNull(this.keyReader);
    list.stream().filter(paramApplication -> (paramApplication.samlv2Configuration.enabled && paramApplication.samlv2Configuration.keyId != null)).map(paramApplication -> paramApplication.samlv2Configuration.keyId).distinct().map(this.keyReader::retrieveById)
      .map(paramKey -> (PEM.decode(paramKey.certificate)).certificate)
      .forEach(paramCertificate -> paramMetaData.idp.certificates.add(paramCertificate));
    return this.samlv2Service.buildMetadataResponse(metaData);
  }
  
  public String buildPostLogoutResponse(SAMLv2ProviderService.FusionAuthLogoutResponse paramFusionAuthLogoutResponse) throws SAMLException {
    generateId((SAMLRequest)paramFusionAuthLogoutResponse);
    Application.SAMLv2Configuration sAMLv2Configuration = paramFusionAuthLogoutResponse.application.samlv2Configuration;
    Key key = this.keyReader.retrieveById(sAMLv2Configuration.logout.keyId);
    PrivateKey privateKey = (PEM.decode(key.privateKey)).privateKey;
    Certificate certificate = (PEM.decode(key.certificate)).certificate;
    return this.samlv2Service.buildPostLogoutResponse(paramFusionAuthLogoutResponse, true, privateKey, (X509Certificate)certificate, toSAMLAlgorithm(key.algorithm), sAMLv2Configuration.logout.xmlSignatureC14nMethod.getURI());
  }
  
  public String buildRedirectLogoutResponse(SAMLv2ProviderService.FusionAuthLogoutResponse paramFusionAuthLogoutResponse, String paramString) throws SAMLException {
    generateId((SAMLRequest)paramFusionAuthLogoutResponse);
    Application.SAMLv2Configuration sAMLv2Configuration = paramFusionAuthLogoutResponse.application.samlv2Configuration;
    Key key = this.keyReader.retrieveById(sAMLv2Configuration.logout.keyId);
    PrivateKey privateKey = (PEM.decode(key.privateKey)).privateKey;
    return this.samlv2Service.buildRedirectLogoutResponse(paramFusionAuthLogoutResponse, paramString, true, privateKey, toSAMLAlgorithm(key.algorithm));
  }
  
  public String buildResponse(SAMLv2ProviderService.FusionAuthAuthenticationResponse paramFusionAuthAuthenticationResponse) throws SAMLException {
    generateId((SAMLRequest)paramFusionAuthAuthenticationResponse);
    Application.SAMLv2Configuration sAMLv2Configuration = paramFusionAuthAuthenticationResponse.application.samlv2Configuration;
    Key key = this.keyReader.retrieveById(sAMLv2Configuration.keyId);
    PrivateKey privateKey = (PEM.decode(key.privateKey)).privateKey;
    Certificate certificate1 = (PEM.decode(key.certificate)).certificate;
    SignatureLocation signatureLocation = (sAMLv2Configuration.xmlSignatureLocation == Application.SAMLv2Configuration.XMLSignatureLocation.Assertion) ? SignatureLocation.Assertion : SignatureLocation.Response;
    _SAMLv2AssertionEncryptionConfiguration _SAMLv2AssertionEncryptionConfiguration = new _SAMLv2AssertionEncryptionConfiguration(sAMLv2Configuration.assertionEncryptionConfiguration);
    Certificate certificate2 = _SAMLv2AssertionEncryptionConfiguration.enabled ? (PEM.decode((this.keyReader.retrieveById(_SAMLv2AssertionEncryptionConfiguration.keyTransportEncryptionKeyId)).certificate)).certificate : null;
    return this.samlv2Service.buildAuthnResponse(paramFusionAuthAuthenticationResponse, true, privateKey, (X509Certificate)certificate1, toSAMLAlgorithm(key.algorithm), sAMLv2Configuration.xmlSignatureC14nMethod
        .getURI(), signatureLocation, true, _SAMLv2AssertionEncryptionConfiguration.enabled, _SAMLv2AssertionEncryptionConfiguration.encryptionAlgorithm, _SAMLv2AssertionEncryptionConfiguration.keyLocation, _SAMLv2AssertionEncryptionConfiguration.keyTransportAlgorithm, (X509Certificate)certificate2, _SAMLv2AssertionEncryptionConfiguration.digestAlgorithm, _SAMLv2AssertionEncryptionConfiguration.maskGenerationFunction);
  }
  
  public String buildSPMetaDataResponse(UUID paramUUID, String paramString) throws SAMLException {
    BaseIdentityProvider baseIdentityProvider = (BaseIdentityProvider)this.identityProviderCache.get(paramUUID);
    if (baseIdentityProvider == null)
      return null; 
    MetaData metaData = new MetaData();
    if (baseIdentityProvider instanceof SAMLv2IdentityProvider) {
      SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)baseIdentityProvider;
      metaData.id = sAMLv2IdentityProvider.id.toString();
      metaData.entityId = SAMLv2Helper.getServiceProviderEntityId(paramString, sAMLv2IdentityProvider);
      metaData.sp = new MetaData.SPMetaData();
      metaData.sp.acsEndpoint = SAMLv2Helper.getACS(paramString);
      metaData.sp.nameIDFormat = sAMLv2IdentityProvider.nameIdFormat;
      metaData.sp.authnRequestsSigned = sAMLv2IdentityProvider.signRequest;
      if (sAMLv2IdentityProvider.signRequest) {
        Key key = this.keyReader.retrieveById(sAMLv2IdentityProvider.requestSigningKeyId);
        metaData.sp.certificates.add((PEM.decode(key.certificate)).certificate);
      } 
    } else if (baseIdentityProvider instanceof SAMLv2IdPInitiatedIdentityProvider) {
      SAMLv2IdPInitiatedIdentityProvider sAMLv2IdPInitiatedIdentityProvider = (SAMLv2IdPInitiatedIdentityProvider)baseIdentityProvider;
      metaData.id = sAMLv2IdPInitiatedIdentityProvider.id.toString();
      metaData.entityId = SAMLv2Helper.getServiceProviderEntityId(paramString, sAMLv2IdPInitiatedIdentityProvider);
      metaData.sp = new MetaData.SPMetaData();
      metaData.sp.acsEndpoint = SAMLv2Helper.getACS(paramString);
      metaData.sp.nameIDFormat = NameIDFormat.EmailAddress.toSAMLFormat();
    } else {
      throw new IllegalStateException("Unexpected IdP type [" + String.valueOf(baseIdentityProvider.getType()) + "]");
    } 
    return this.samlv2Service.buildMetadataResponse(metaData);
  }
  
  public Map<UUID, URI> getSessionParticipantLogoutURLs(UUID paramUUID, Application paramApplication, String paramString) throws SAMLException {
    Map<UUID, URI> map = (Map)this.applicationCache.getAllByTenantId(paramUUID).stream().filter(paramApplication -> paramApplication.samlv2Configuration.enabled).filter(paramApplication2 -> !paramApplication2.id.equals(paramApplication1.id)).filter(paramApplication -> paramApplication.samlv2Configuration.logout.singleLogout.enabled).collect(Collectors.toMap(paramApplication -> paramApplication.id, paramApplication -> paramApplication.samlv2Configuration.logout.singleLogout.url));
    for (UUID uUID : map.keySet()) {
      LogoutRequest logoutRequest = new LogoutRequest();
      String str1 = ((URI)map.get(uUID)).toString();
      logoutRequest.destination = str1;
      generateId((SAMLRequest)logoutRequest);
      logoutRequest.issuer = SAMLv2Helper.getIdentityProviderEntityId(paramString, paramUUID);
      Application.SAMLv2Configuration sAMLv2Configuration = paramApplication.samlv2Configuration;
      Key key = this.keyReader.retrieveById(sAMLv2Configuration.logout.singleLogout.keyId);
      PrivateKey privateKey = (PEM.decode(key.privateKey)).privateKey;
      String str2 = str1 + "?" + str1;
      map.put(uUID, URI.create(str2));
    } 
    return map;
  }
  
  public void logSAMLRequest(boolean paramBoolean, Binding paramBinding, String paramString1, SAMLRequest paramSAMLRequest, String paramString2) {
    if (paramBoolean) {
      String str = (paramBinding == Binding.HTTP_Redirect) ? "Deflated and encoded" : "Encoded";
      EventLogHelper.create(new EventLog(EventLogType.Debug, "Incoming SAML v2 " + paramString1 + ".\n\nBinding:\n" + paramBinding
            .toSAMLFormat() + "\n\n" + str + " request:\n" + paramString2 + "\n\nDecoded XML request:\n" + (
            
            (paramSAMLRequest.xml == null) ? " XML not available." : XMLTools.prettyPrint(paramSAMLRequest.xml)) + "\n"));
    } 
  }
  
  public void logSAMLResponse(boolean paramBoolean, Binding paramBinding, String paramString1, SAMLRequest paramSAMLRequest, String paramString2) {
    if (paramBoolean)
      try {
        String str1 = (paramBinding == Binding.HTTP_Redirect) ? "Deflated and encoded" : "Encoded";
        String str2 = (paramBinding == Binding.HTTP_Redirect) ? URLDecoder.decode(paramString2.substring("SAMLResponse=".length()).split("&")[0], StandardCharsets.UTF_8) : paramString2;
        paramSAMLRequest
          
          .xml = new String((paramBinding == Binding.HTTP_Redirect) ? SAMLTools.decodeAndInflate(str2) : SAMLTools.decode(str2), StandardCharsets.UTF_8);
        EventLogHelper.create(new EventLog(EventLogType.Debug, paramString1 + " being sent to the service provider.\n" + paramString1 + "\n\nBinding:\n" + paramSAMLRequest.destination + "\n\n" + paramBinding
              
              .toSAMLFormat() + " response:\n" + str1 + "\n\nUn-encoded XML response:\n" + str2 + "\n"));
      } catch (SAMLException sAMLException) {
        EventLogHelper.create(new EventLog(EventLogType.Error, "This is unexpected, we failed to decode a logout response we built.", (Throwable)sAMLException));
        throw new ErrorException(new Object[0]);
      }  
  }
  
  public AuthenticationRequest parseAuthNPostRequest(UUID paramUUID, String paramString) throws SAMLException {
    return this.samlv2Service.parseRequestPostBinding(paramString, paramAuthenticationRequest -> {
          Objects.requireNonNull(this.keyCache);
          return new FusionAuthPostBindingSignatureHelper((), paramAuthenticationRequest.issuer, this.keyCache::getSAMLKeySelector, (), ());
        });
  }
  
  public AuthenticationRequest parseAuthNRedirectRequest(UUID paramUUID, String paramString) throws SAMLException {
    return this.samlv2Service.parseRequestRedirectBinding(paramString, paramAuthenticationRequest -> {
          Objects.requireNonNull(this.keyCache);
          return new FusionAuthRedirectBindingSignatureHelper((), paramAuthenticationRequest.issuer, this.keyCache::getPublicKey, (), ());
        });
  }
  
  public LogoutRequest parseLogoutPostRequest(UUID paramUUID, String paramString) throws SAMLException {
    return this.samlv2Service.parseLogoutRequestPostBinding(paramString, paramLogoutRequest -> {
          Objects.requireNonNull(this.keyCache);
          return new FusionAuthPostBindingSignatureHelper((), paramLogoutRequest.issuer, this.keyCache::getSAMLKeySelector, (), ());
        });
  }
  
  public LogoutRequest parseLogoutRedirectRequest(UUID paramUUID, String paramString) throws SAMLException {
    return this.samlv2Service.parseLogoutRequestRedirectBinding(paramString, paramLogoutRequest -> {
          Objects.requireNonNull(this.keyCache);
          return new FusionAuthRedirectBindingSignatureHelper((), paramLogoutRequest.issuer, this.keyCache::getPublicKey, (), ());
        });
  }
  
  public void populateErrorResponse(SAMLv2ProviderService.FusionAuthAuthenticationResponse paramFusionAuthAuthenticationResponse, AuthenticationRequest paramAuthenticationRequest, String paramString) {
    populateResponseWithoutAssertions(paramFusionAuthAuthenticationResponse, paramAuthenticationRequest, paramString, ZonedDateTime.now(ZoneOffset.UTC));
  }
  
  public void populateResponse(SAMLv2ProviderService.FusionAuthAuthenticationResponse paramFusionAuthAuthenticationResponse, AuthenticationRequest paramAuthenticationRequest, String paramString, User paramUser) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    populateResponseWithoutAssertions(paramFusionAuthAuthenticationResponse, paramAuthenticationRequest, paramString, zonedDateTime);
    Application.SAMLv2Configuration sAMLv2Configuration = paramFusionAuthAuthenticationResponse.application.samlv2Configuration;
    String str1 = (sAMLv2Configuration.audience != null) ? sAMLv2Configuration.audience : sAMLv2Configuration.issuer;
    Assertion assertion = new Assertion();
    assertion.conditions = new Conditions();
    assertion.conditions.audiences.add(str1);
    assertion.conditions.notBefore = zonedDateTime.minusMinutes(3L);
    assertion.conditions.notOnOrAfter = zonedDateTime.plusMinutes(30L);
    assertion.subject = new Subject();
    assertion.subject.subjectConfirmation = new SubjectConfirmation();
    assertion.subject.subjectConfirmation.inResponseTo = paramAuthenticationRequest.id;
    assertion.subject.subjectConfirmation.method = ConfirmationMethod.Bearer;
    assertion.subject.subjectConfirmation.notOnOrAfter = zonedDateTime.plusHours(1L);
    assertion.subject.subjectConfirmation.recipient = paramFusionAuthAuthenticationResponse.destination;
    NameID nameID = new NameID();
    String str2 = paramUser.email;
    if (paramAuthenticationRequest.nameIdFormat.equals(NameIDFormat.Persistent.toSAMLFormat())) {
      nameID.format = NameIDFormat.Persistent.toSAMLFormat();
      nameID.id = paramUser.id.toString();
    } else {
      nameID.format = NameIDFormat.EmailAddress.toSAMLFormat();
      nameID.id = str2;
    } 
    assertion.subject.nameIDs = List.of(nameID);
    addAttribute(assertion, paramUser.id, "id");
    addAttribute(assertion, paramUser.birthDate, StandardClaim.DateOfBirth.getName());
    addAttribute(assertion, paramUser.birthDate, "date_of_birth");
    addAttribute(assertion, paramUser.birthDate, "birthdate");
    addAttribute(assertion, str2, StandardClaim.EmailAddress.getName());
    addAttribute(assertion, str2, "email");
    addAttribute(assertion, paramUser.firstName, StandardClaim.FirstName.getName());
    addAttribute(assertion, paramUser.firstName, "first_name");
    addAttribute(assertion, paramUser.fullName, StandardClaim.FullName.getName());
    addAttribute(assertion, paramUser.fullName, "name");
    addAttribute(assertion, paramUser.fullName, "full_name");
    addAttribute(assertion, paramUser.lastName, StandardClaim.LastName.getName());
    addAttribute(assertion, paramUser.lastName, "last_name");
    addAttribute(assertion, paramUser.mobilePhone, StandardClaim.MobilePhone.getName());
    addAttribute(assertion, paramUser.mobilePhone, "mobile_phone");
    paramFusionAuthAuthenticationResponse.assertions.add(assertion);
    if (paramFusionAuthAuthenticationResponse.application.lambdaConfiguration.samlv2PopulateId != null) {
      paramFusionAuthAuthenticationResponse.assertion = assertion;
      paramFusionAuthAuthenticationResponse.assertions = null;
      UserRegistration userRegistration = paramUser.getRegistrationForApplication(paramFusionAuthAuthenticationResponse.application.id);
      this.lambdaInvocationService.invoke(paramFusionAuthAuthenticationResponse.application.lambdaConfiguration.samlv2PopulateId, new LambdaArgument[] { new MutableLambdaArgument(paramFusionAuthAuthenticationResponse), new ImmutableLambdaArgument((new User(paramUser))
              
              .secure()), new ImmutableLambdaArgument(userRegistration) });
      paramFusionAuthAuthenticationResponse.assertions = List.of(paramFusionAuthAuthenticationResponse.assertion);
    } 
  }
  
  public String resolveACS(Application paramApplication, AuthenticationRequest paramAuthenticationRequest) {
    String str = ((URI)paramApplication.samlv2Configuration.authorizedRedirectURLs.get(0)).toString();
    if (paramApplication.samlv2Configuration.authorizedRedirectURLs.stream().map(URI::toString).anyMatch(paramString -> paramString.equals(paramAuthenticationRequest.acsURL)))
      str = paramAuthenticationRequest.acsURL; 
    return str;
  }
  
  public String resolveACSDuringIdPInitiatedLoginRequest(Application paramApplication, String paramString1, String paramString2) {
    String str = null;
    if (paramString2 != null)
      try {
        URI uRI = URI.create(paramString2);
        if (paramApplication.samlv2Configuration.authorizedRedirectURLs.contains(uRI))
          str = paramString2; 
      } catch (Exception exception) {} 
    if (str == null)
      if (paramString1 != null) {
        try {
          URI uRI = URI.create(paramString1);
          if (paramApplication.samlv2Configuration.authorizedRedirectURLs.contains(uRI))
            str = paramString1; 
        } catch (Exception exception) {}
      } else {
        str = paramApplication.samlv2Configuration.authorizedRedirectURLs.isEmpty() ? null : ((URI)paramApplication.samlv2Configuration.authorizedRedirectURLs.getFirst()).toString();
      }  
    return str;
  }
  
  public Application resolveApplication(UUID paramUUID, String paramString) {
    return this.applicationReader.retrieveBySAMLv2Issuer(paramUUID, paramString);
  }
  
  public SAMLv2ProviderService.FusionAuthAuthenticationResponse validateAuthnRequest(SAMLRequest paramSAMLRequest, UUID paramUUID) {
    return (SAMLv2ProviderService.FusionAuthAuthenticationResponse)validateRequest(paramSAMLRequest, paramUUID, "AuthnRequest");
  }
  
  public SAMLv2ProviderService.FusionAuthAuthenticationResponse validateInitiatedLoginRequest(Application paramApplication, String paramString1, String paramString2) {
    String str = resolveACSDuringIdPInitiatedLoginRequest(paramApplication, paramString1, paramString2);
    SAMLv2ProviderService.FusionAuthAuthenticationResponse fusionAuthAuthenticationResponse = new SAMLv2ProviderService.FusionAuthAuthenticationResponse();
    fusionAuthAuthenticationResponse.application = paramApplication;
    fusionAuthAuthenticationResponse.status.code = ResponseStatus.Requester;
    if (!paramApplication.samlv2Configuration.enabled) {
      fusionAuthAuthenticationResponse.status.message = "SAML v2 login is disabled.";
    } else if (!paramApplication.samlv2Configuration.initiatedLogin.enabled) {
      fusionAuthAuthenticationResponse.status.message = "SAML v2 IdP initiated login is disabled.";
    } else if (str == null) {
      fusionAuthAuthenticationResponse.status
        
        .message = (paramString1 != null) ? ("Invalid AssertionConsumerServiceURL [" + paramString1 + "].") : "The AssertionConsumerServiceURL is required.";
    } else {
      return null;
    } 
    return fusionAuthAuthenticationResponse;
  }
  
  public SAMLv2ProviderService.FusionAuthLogoutResponse validateLogoutRequest(SAMLRequest paramSAMLRequest, UUID paramUUID) {
    return (SAMLv2ProviderService.FusionAuthLogoutResponse)validateRequest(paramSAMLRequest, paramUUID, "LogoutRequest");
  }
  
  public String validateRedirectBinding(String paramString1, String paramString2) {
    if (paramString1 != null && paramString2 == null)
      return "The SAML login request contained the [SigAlg] request parameter, but is missing the [Signature] request parameter. When specifying the [SigAlg] request parameter, you must also provide the [Signature] request parameter."; 
    if (paramString1 == null && paramString2 != null)
      return "The SAML login request included the [Signature] request parameter, but is missing the [SigAlg] request parameter. When specifying the [Signature] request parameter, you must also provide the [SigAlg] request parameter."; 
    return null;
  }
  
  private void addAttribute(Assertion paramAssertion, Object paramObject, String paramString) {
    if (paramObject != null)
      ((List<String>)paramAssertion.attributes.computeIfAbsent(paramString, paramString -> new ArrayList())).add(paramObject.toString()); 
  }
  
  private void generateId(SAMLRequest paramSAMLRequest) {
    if (paramSAMLRequest.id == null)
      paramSAMLRequest.id = "_" + String.valueOf(UUID.randomUUID()); 
  }
  
  private void populateResponseWithoutAssertions(SAMLv2ProviderService.FusionAuthAuthenticationResponse paramFusionAuthAuthenticationResponse, AuthenticationRequest paramAuthenticationRequest, String paramString, ZonedDateTime paramZonedDateTime) {
    paramFusionAuthAuthenticationResponse.id = "_" + String.valueOf(UUID.randomUUID());
    if (paramAuthenticationRequest != null) {
      paramFusionAuthAuthenticationResponse.destination = resolveACS(paramFusionAuthAuthenticationResponse.application, paramAuthenticationRequest);
      paramFusionAuthAuthenticationResponse.inResponseTo = paramAuthenticationRequest.id;
    } 
    paramFusionAuthAuthenticationResponse.issueInstant = paramZonedDateTime;
    paramFusionAuthAuthenticationResponse.sessionIndex = paramString;
    paramFusionAuthAuthenticationResponse.version = "2.0";
  }
  
  private Algorithm toSAMLAlgorithm(Key.KeyAlgorithm paramKeyAlgorithm) {
    String str = paramKeyAlgorithm.name();
    return Algorithm.valueOf(str);
  }
  
  private SAMLResponse validateRequest(SAMLRequest paramSAMLRequest, UUID paramUUID, String paramString) {
    SAMLv2ProviderService.FusionAuthLogoutResponse fusionAuthLogoutResponse = (SAMLv2ProviderService.FusionAuthLogoutResponse)((paramSAMLRequest instanceof LogoutRequest) ? new SAMLv2ProviderService.FusionAuthLogoutResponse() : new SAMLv2ProviderService.FusionAuthAuthenticationResponse());
    Application application = null;
    try {
      application = resolveApplication(paramUUID, paramSAMLRequest.issuer);
      if (application == null) {
        ((SAMLResponse)fusionAuthLogoutResponse).status.code = ResponseStatus.Requester;
        ((SAMLResponse)fusionAuthLogoutResponse).status.message = "The " + paramString + " contained an invalid issuer [" + paramSAMLRequest.issuer + "] that does not map to an Application in FusionAuth";
      } else if (!application.samlv2Configuration.enabled) {
        application = null;
        ((SAMLResponse)fusionAuthLogoutResponse).status.code = ResponseStatus.Requester;
        ((SAMLResponse)fusionAuthLogoutResponse).status.message = "The " + paramString + " contained an invalid issuer [" + paramSAMLRequest.issuer + "]. That Application does not have SAML enabled";
      } else if (!paramSAMLRequest.version.equals("2.0")) {
        ((SAMLResponse)fusionAuthLogoutResponse).status.code = ResponseStatus.VersionMismatch;
        ((SAMLResponse)fusionAuthLogoutResponse).status.message = "The " + paramString + " contained an invalid version [" + paramSAMLRequest.version + "]. FusionAuth only supports SAML v2.0";
      } else if (paramSAMLRequest instanceof AuthenticationRequest && !SupportedNameIdFormats.contains(((AuthenticationRequest)paramSAMLRequest).nameIdFormat)) {
        ((SAMLResponse)fusionAuthLogoutResponse).status.code = ResponseStatus.InvalidNameIDPolicy;
        ((SAMLResponse)fusionAuthLogoutResponse).status.message = "The " + paramString + " contained an invalid NameID policy. FusionAuth only supports the following NameID formats [" + String.join(", ", (Iterable)SupportedNameIdFormats) + "]";
      } else if (paramSAMLRequest instanceof AuthenticationRequest && ((AuthenticationRequest)paramSAMLRequest).acsURL != null && application.samlv2Configuration.authorizedRedirectURLs.stream().map(URI::toString).noneMatch(paramString -> paramString.equals(((AuthenticationRequest)paramSAMLRequest).acsURL))) {
        ((SAMLResponse)fusionAuthLogoutResponse).status.code = ResponseStatus.Requester;
        ((SAMLResponse)fusionAuthLogoutResponse).status.message = "The " + paramString + " contained an invalid AssertionConsumerServiceURL [" + ((AuthenticationRequest)paramSAMLRequest).acsURL + "]";
      } else if (paramSAMLRequest instanceof LogoutRequest && !SupportedNameIdFormats.contains(((LogoutRequest)paramSAMLRequest).nameIdFormat)) {
        ((SAMLResponse)fusionAuthLogoutResponse).status.code = ResponseStatus.InvalidNameIDPolicy;
        ((SAMLResponse)fusionAuthLogoutResponse).status.message = "The " + paramString + " contained an invalid NameID policy. FusionAuth only supports the following NameID formats [" + String.join(", ", (Iterable)SupportedNameIdFormats) + "]";
      } else {
        ((SAMLResponse)fusionAuthLogoutResponse).status.code = ResponseStatus.Success;
      } 
    } catch (IllegalArgumentException illegalArgumentException) {
      ((SAMLResponse)fusionAuthLogoutResponse).status.code = ResponseStatus.Requester;
      ((SAMLResponse)fusionAuthLogoutResponse).status.message = "The " + paramString + " contained an invalid issuer [" + paramSAMLRequest.issuer + "] that does not map to an Application in FusionAuth";
    } 
    if (fusionAuthLogoutResponse instanceof SAMLv2ProviderService.FusionAuthLogoutResponse) {
      SAMLv2ProviderService.FusionAuthLogoutResponse fusionAuthLogoutResponse1 = fusionAuthLogoutResponse;
      fusionAuthLogoutResponse1.application = application;
    } else if (fusionAuthLogoutResponse instanceof SAMLv2ProviderService.FusionAuthAuthenticationResponse) {
      SAMLv2ProviderService.FusionAuthAuthenticationResponse fusionAuthAuthenticationResponse = (SAMLv2ProviderService.FusionAuthAuthenticationResponse)fusionAuthLogoutResponse;
      fusionAuthAuthenticationResponse.application = application;
    } 
    return (SAMLResponse)fusionAuthLogoutResponse;
  }
  
  private static class _SAMLv2AssertionEncryptionConfiguration {
    public DigestAlgorithm digestAlgorithm;
    
    public boolean enabled;
    
    public EncryptionAlgorithm encryptionAlgorithm;
    
    public KeyLocation keyLocation;
    
    public KeyTransportAlgorithm keyTransportAlgorithm;
    
    public UUID keyTransportEncryptionKeyId;
    
    public MaskGenerationFunction maskGenerationFunction;
    
    public _SAMLv2AssertionEncryptionConfiguration(Application.SAMLv2Configuration.SAMLv2AssertionEncryptionConfiguration param1SAMLv2AssertionEncryptionConfiguration) {
      this.digestAlgorithm = DigestAlgorithm.valueOf(param1SAMLv2AssertionEncryptionConfiguration.digestAlgorithm);
      this.enabled = param1SAMLv2AssertionEncryptionConfiguration.enabled;
      this.encryptionAlgorithm = EncryptionAlgorithm.valueOf(param1SAMLv2AssertionEncryptionConfiguration.encryptionAlgorithm);
      this.keyLocation = KeyLocation.valueOf(param1SAMLv2AssertionEncryptionConfiguration.keyLocation);
      this.keyTransportAlgorithm = KeyTransportAlgorithm.valueOf(param1SAMLv2AssertionEncryptionConfiguration.keyTransportAlgorithm);
      this.keyTransportEncryptionKeyId = param1SAMLv2AssertionEncryptionConfiguration.keyTransportEncryptionKeyId;
      this.maskGenerationFunction = MaskGenerationFunction.valueOf(param1SAMLv2AssertionEncryptionConfiguration.maskGenerationFunction);
    }
  }
}
