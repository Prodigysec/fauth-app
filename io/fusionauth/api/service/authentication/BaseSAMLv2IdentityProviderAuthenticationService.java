package io.fusionauth.api.service.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inversoft.rest.ProxyInfoSupplier;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityProviderLinkMapper;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.security.SAMLKeySelector;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.identity.IdentityProviderUserService;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.system.EventLogService;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.UserMetricsService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.api.util.XMLTools;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import io.fusionauth.domain.provider.BaseSAMLv2IdentityProvider;
import io.fusionauth.pem.domain.PEM;
import io.fusionauth.samlv2.domain.Assertion;
import io.fusionauth.samlv2.domain.AuthenticationResponse;
import io.fusionauth.samlv2.domain.ResponseStatus;
import io.fusionauth.samlv2.domain.SAMLException;
import io.fusionauth.samlv2.service.SAMLv2Service;
import io.fusionauth.samlv2.util.SAMLTools;
import java.security.PrivateKey;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public abstract class BaseSAMLv2IdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  protected final KeyCache keyCache;
  
  protected final KeyReaderService keyReader;
  
  protected final ObjectMapper objectMapper;
  
  protected final SAMLv2Service samlService;
  
  public BaseSAMLv2IdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, KeyCache paramKeyCache, KeyReaderService paramKeyReaderService, LambdaInvocationService paramLambdaInvocationService, ObjectMapper paramObjectMapper, SAMLv2Service paramSAMLv2Service, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
    this.keyCache = paramKeyCache;
    this.keyReader = paramKeyReaderService;
    this.objectMapper = paramObjectMapper;
    this.samlService = paramSAMLv2Service;
  }
  
  protected InternalSAMLAuthenticationResponse handleIdpInitiatedAssertions(Tenant paramTenant, Application paramApplication, InternalSAMLAuthenticationResponse paramInternalSAMLAuthenticationResponse, BaseIdentityProviderAuthenticationService.LoginContext paramLoginContext, BaseSAMLv2IdentityProvider<?> paramBaseSAMLv2IdentityProvider, String paramString) {
    // Byte code:
    //   0: aload #4
    //   2: getfield debugger : Lio/fusionauth/api/service/system/eventLog/Debugger;
    //   5: astore #7
    //   7: aload_3
    //   8: getfield assertions : Ljava/util/List;
    //   11: invokeinterface stream : ()Ljava/util/stream/Stream;
    //   16: <illegal opcode> apply : ()Ljava/util/function/Function;
    //   21: invokeinterface map : (Ljava/util/function/Function;)Ljava/util/stream/Stream;
    //   26: invokeinterface toList : ()Ljava/util/List;
    //   31: astore #8
    //   33: aload #7
    //   35: ldc ', '
    //   37: aload #8
    //   39: invokestatic join : (Ljava/lang/CharSequence;Ljava/lang/Iterable;)Ljava/lang/String;
    //   42: <illegal opcode> makeConcatWithConstants : (Ljava/lang/String;)Ljava/lang/String;
    //   47: invokevirtual log : (Ljava/lang/String;)Lio/fusionauth/api/service/system/eventLog/Debugger;
    //   50: pop
    //   51: aload #8
    //   53: invokeinterface iterator : ()Ljava/util/Iterator;
    //   58: astore #9
    //   60: aload #9
    //   62: invokeinterface hasNext : ()Z
    //   67: ifeq -> 138
    //   70: aload #9
    //   72: invokeinterface next : ()Ljava/lang/Object;
    //   77: checkcast java/lang/String
    //   80: astore #10
    //   82: aload_0
    //   83: getfield externalIdentifierReader : Lio/fusionauth/api/service/user/ExternalIdentifierReaderService;
    //   86: aconst_null
    //   87: aload #10
    //   89: iconst_1
    //   90: anewarray io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType
    //   93: dup
    //   94: iconst_0
    //   95: getstatic io/fusionauth/api/domain/ExternalIdentifier$ExternalIdType.SAMLv2IdPInitiatedAuthNResponse : Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;
    //   98: aastore
    //   99: invokeinterface validate : (Lio/fusionauth/domain/Tenant;Ljava/lang/String;[Lio/fusionauth/api/domain/ExternalIdentifier$ExternalIdType;)Lio/fusionauth/api/service/user/ExternalIdentifierReaderService$ValidationResult;
    //   104: getfield id : Lio/fusionauth/api/domain/ExternalIdentifier;
    //   107: astore #11
    //   109: aload #11
    //   111: ifnull -> 135
    //   114: aload #7
    //   116: ldc 'This SAML response has already been processed and may not be used a second time.'
    //   118: invokevirtual log : (Ljava/lang/String;)Lio/fusionauth/api/service/system/eventLog/Debugger;
    //   121: invokevirtual done : ()V
    //   124: new io/fusionauth/api/service/authentication/ExternalAuthenticationException
    //   127: dup
    //   128: getstatic io/fusionauth/api/service/authentication/ExternalAuthenticationException$Reason.SAMLResponseUnexpectedOrReplayed : Lio/fusionauth/api/service/authentication/ExternalAuthenticationException$Reason;
    //   131: invokespecial <init> : (Lio/fusionauth/api/service/authentication/ExternalAuthenticationException$Reason;)V
    //   134: athrow
    //   135: goto -> 60
    //   138: aload #5
    //   140: instanceof io/fusionauth/domain/provider/SAMLv2IdentityProvider
    //   143: ifeq -> 183
    //   146: aload #5
    //   148: checkcast io/fusionauth/domain/provider/SAMLv2IdentityProvider
    //   151: astore #9
    //   153: aload #9
    //   155: getfield issuer : Ljava/lang/String;
    //   158: ifnull -> 183
    //   161: aload #7
    //   163: aload #4
    //   165: getfield connectionTestId : Lio/fusionauth/api/domain/ExternalIdentifier;
    //   168: aload_3
    //   169: getfield assertion : Lio/fusionauth/samlv2/domain/Assertion;
    //   172: aload #9
    //   174: getfield issuer : Ljava/lang/String;
    //   177: invokestatic assertAudience : (Lio/fusionauth/api/service/system/eventLog/Debugger;Lio/fusionauth/api/domain/ExternalIdentifier;Lio/fusionauth/samlv2/domain/Assertion;Ljava/lang/String;)V
    //   180: goto -> 210
    //   183: aload #7
    //   185: aload #4
    //   187: getfield connectionTestId : Lio/fusionauth/api/domain/ExternalIdentifier;
    //   190: aload_3
    //   191: getfield assertion : Lio/fusionauth/samlv2/domain/Assertion;
    //   194: aload #5
    //   196: getfield id : Ljava/util/UUID;
    //   199: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   202: <illegal opcode> makeConcatWithConstants : (Ljava/lang/String;)Ljava/lang/String;
    //   207: invokestatic assertAudienceSuffix : (Lio/fusionauth/api/service/system/eventLog/Debugger;Lio/fusionauth/api/domain/ExternalIdentifier;Lio/fusionauth/samlv2/domain/Assertion;Ljava/lang/String;)V
    //   210: aload #7
    //   212: aload_3
    //   213: aload #6
    //   215: invokestatic assertIssuer : (Lio/fusionauth/api/service/system/eventLog/Debugger;Lio/fusionauth/samlv2/domain/AuthenticationResponse;Ljava/lang/String;)V
    //   218: aload #7
    //   220: aload #4
    //   222: getfield connectionTestId : Lio/fusionauth/api/domain/ExternalIdentifier;
    //   225: aload_3
    //   226: getfield assertion : Lio/fusionauth/samlv2/domain/Assertion;
    //   229: getfield subject : Lio/fusionauth/samlv2/domain/Subject;
    //   232: invokestatic assertSubjectConditions : (Lio/fusionauth/api/service/system/eventLog/Debugger;Lio/fusionauth/api/domain/ExternalIdentifier;Lio/fusionauth/samlv2/domain/Subject;)V
    //   235: aload_0
    //   236: aload_3
    //   237: aload #4
    //   239: aload #5
    //   241: invokevirtual updateContext : (Lio/fusionauth/api/service/authentication/BaseSAMLv2IdentityProviderAuthenticationService$InternalSAMLAuthenticationResponse;Lio/fusionauth/api/service/authentication/BaseIdentityProviderAuthenticationService$LoginContext;Lio/fusionauth/domain/provider/BaseSAMLv2IdentityProvider;)V
    //   244: aload_3
    //   245: getfield assertion : Lio/fusionauth/samlv2/domain/Assertion;
    //   248: getfield conditions : Lio/fusionauth/samlv2/domain/Conditions;
    //   251: ifnull -> 280
    //   254: aload_3
    //   255: getfield assertion : Lio/fusionauth/samlv2/domain/Assertion;
    //   258: getfield conditions : Lio/fusionauth/samlv2/domain/Conditions;
    //   261: getfield notOnOrAfter : Ljava/time/ZonedDateTime;
    //   264: ifnull -> 280
    //   267: aload_3
    //   268: getfield assertion : Lio/fusionauth/samlv2/domain/Assertion;
    //   271: getfield conditions : Lio/fusionauth/samlv2/domain/Conditions;
    //   274: getfield notOnOrAfter : Ljava/time/ZonedDateTime;
    //   277: goto -> 292
    //   280: getstatic java/time/ZoneOffset.UTC : Ljava/time/ZoneOffset;
    //   283: invokestatic now : (Ljava/time/ZoneId;)Ljava/time/ZonedDateTime;
    //   286: ldc2_w 30
    //   289: invokevirtual plusMinutes : (J)Ljava/time/ZonedDateTime;
    //   292: astore #9
    //   294: aload_3
    //   295: getfield assertions : Ljava/util/List;
    //   298: aload_0
    //   299: aload_1
    //   300: aload_2
    //   301: aload #9
    //   303: <illegal opcode> accept : (Lio/fusionauth/api/service/authentication/BaseSAMLv2IdentityProviderAuthenticationService;Lio/fusionauth/domain/Tenant;Lio/fusionauth/domain/Application;Ljava/time/ZonedDateTime;)Ljava/util/function/Consumer;
    //   308: invokeinterface forEach : (Ljava/util/function/Consumer;)V
    //   313: aload_3
    //   314: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #95	-> 0
    //   #97	-> 7
    //   #98	-> 33
    //   #100	-> 51
    //   #101	-> 82
    //   #102	-> 109
    //   #104	-> 114
    //   #105	-> 121
    //   #106	-> 124
    //   #108	-> 135
    //   #110	-> 138
    //   #111	-> 161
    //   #113	-> 183
    //   #116	-> 210
    //   #119	-> 218
    //   #121	-> 235
    //   #124	-> 244
    //   #125	-> 267
    //   #126	-> 280
    //   #128	-> 294
    //   #130	-> 313
  }
  
  protected void handleLoginExceptions(Debugger paramDebugger, AuthenticationResponse paramAuthenticationResponse, Exception paramException, @Nullable ExternalIdentifier paramExternalIdentifier) {
    paramDebugger.log("Caught an exception, unable to complete the request. See the Error Event Log with the full XML response.")
      .done();
    if (paramException instanceof ExternalAuthenticationException) {
      ExternalAuthenticationException externalAuthenticationException = (ExternalAuthenticationException)paramException;
      throw externalAuthenticationException;
    } 
    if (paramExternalIdentifier == null)
      this.eventLogService.create(new EventLog(EventLogType.Error, "Unable to parse or validate SAML response.\n" + (
            
            (paramAuthenticationResponse != null) ? ("SAML Response:\n" + XMLTools.prettyPrint(paramAuthenticationResponse.rawResponse)) : ""), paramException)); 
  }
  
  protected InternalSAMLAuthenticationResponse parseSamlResponse(Debugger paramDebugger, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, BaseSAMLv2IdentityProvider<?> paramBaseSAMLv2IdentityProvider, @Nullable ExternalIdentifier paramExternalIdentifier) throws SAMLException {
    paramDebugger.log("Parse the SAML response");
    String str = paramIdentityProviderLoginRequest.data.get("samlResponse");
    paramDebugger.log("Base64 encoded response:\n" + str);
    try {
      paramDebugger.log("Raw SAML Response:\n" + XMLTools.prettyPrint(SAMLTools.decodeToString(str)));
    } catch (Exception exception) {}
    SAMLKeySelector sAMLKeySelector = this.keyCache.getSAMLKeySelector(paramBaseSAMLv2IdentityProvider.verificationKeyIds);
    PrivateKey privateKey = null;
    if (paramBaseSAMLv2IdentityProvider.assertionDecryptionConfiguration.enabled) {
      Key key = this.keyReader.retrieveById(paramBaseSAMLv2IdentityProvider.assertionDecryptionConfiguration.keyTransportDecryptionKeyId);
      privateKey = (PEM.decode(key.privateKey)).privateKey;
    } 
    AuthenticationResponse authenticationResponse = this.samlService.parseResponse(str, true, sAMLKeySelector, paramBaseSAMLv2IdentityProvider.assertionDecryptionConfiguration.enabled, privateKey);
    if (authenticationResponse.status.code != ResponseStatus.Success) {
      if (paramExternalIdentifier == null) {
        this.eventLogService.create(new EventLog(EventLogType.Error, "The SAML response was not successful.\nStatusCode : " + authenticationResponse.status.code
              .toSAMLFormat() + "\n\nSAML Response:\n\n" + 
              
              XMLTools.prettyPrint(authenticationResponse.rawResponse)));
      } else {
        paramExternalIdentifier.data.addTraceStep("Response status", false, String.format("The SAML authentication response status [%s] indicates the request failed.", new Object[] { authenticationResponse.status.code.toSAMLFormat() }));
      } 
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLResponseStatus);
    } 
    if (authenticationResponse.assertions == null || authenticationResponse.assertions.isEmpty()) {
      if (paramExternalIdentifier == null) {
        this.eventLogService.create(new EventLog(EventLogType.Error, "The SAML response was not successful.\nThe response did not contain any assertions.\n\nSAML Response:\n\n" + 

              
              XMLTools.prettyPrint(authenticationResponse.rawResponse)));
      } else {
        paramExternalIdentifier.data.addTraceStep("Missing assertion", false, "The SAML authentication response did not contain any assertions.");
      } 
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLResponseMissingAssertion);
    } 
    Assertion assertion = new Assertion((Assertion)authenticationResponse.assertions.getFirst());
    if (!authenticationResponse.assertions.stream().allMatch(paramAssertion2 -> 
        (paramAssertion2.subject.equals(paramAssertion1.subject) && paramAssertion2.conditions.equals(paramAssertion1.conditions)))) {
      if (paramExternalIdentifier == null) {
        this.eventLogService.create(new EventLog(EventLogType.Error, "The SAML response was not successful.\nThe response contained multiple assertions whose Subject or Conditions did not match.\n\nSAML Response:\n\n" + 

              
              XMLTools.prettyPrint(authenticationResponse.rawResponse)));
      } else {
        paramExternalIdentifier.data.addTraceStep("Incompatible assertions", false, "The SAML authentication response contained multiple assertions that were incompatible.");
      } 
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLResponseMismatchedAssertions);
    } 
    for (int i = authenticationResponse.assertions.size() - 1; i > 0; i--) {
      if (((Assertion)authenticationResponse.assertions.get(i)).issuer != null) {
        assertion.issuer = ((Assertion)authenticationResponse.assertions.get(i)).issuer;
        break;
      } 
    } 
    assertion.attributes = new HashMap<>();
    authenticationResponse.assertions
      .forEach(paramAssertion2 -> paramAssertion2.attributes.forEach(()));
    paramDebugger.log("Response Ok. Status [" + authenticationResponse.status.code.toSAMLFormat() + "]" + (
        (authenticationResponse.status.message == null) ? "" : (" Message : " + authenticationResponse.status.message)))
      .log("Raw SAML Response:\n" + XMLTools.prettyPrint(authenticationResponse.rawResponse));
    return new InternalSAMLAuthenticationResponse(authenticationResponse, assertion);
  }
  
  protected void updateContext(InternalSAMLAuthenticationResponse paramInternalSAMLAuthenticationResponse, BaseIdentityProviderAuthenticationService.LoginContext paramLoginContext, BaseSAMLv2IdentityProvider<?> paramBaseSAMLv2IdentityProvider) {
    String str = SAMLv2IdPHelper.resolveNameId(paramInternalSAMLAuthenticationResponse.assertion, paramLoginContext);
    paramLoginContext.identityProviderUserId = str;
    if (paramBaseSAMLv2IdentityProvider.uniqueIdClaim != null)
      paramLoginContext.identityProviderUserId = SAMLv2IdPHelper.getAttribute(paramInternalSAMLAuthenticationResponse.assertion, paramBaseSAMLv2IdentityProvider.uniqueIdClaim); 
    if (paramBaseSAMLv2IdentityProvider.useNameIdForEmail) {
      paramLoginContext.email = str;
    } else if (paramBaseSAMLv2IdentityProvider.emailClaim != null) {
      paramLoginContext.email = SAMLv2IdPHelper.getAttribute(paramInternalSAMLAuthenticationResponse.assertion, paramBaseSAMLv2IdentityProvider.emailClaim);
    } 
    paramLoginContext.username = SAMLv2IdPHelper.getAttribute(paramInternalSAMLAuthenticationResponse.assertion, paramBaseSAMLv2IdentityProvider.usernameClaim);
  }
  
  public static class InternalSAMLAuthenticationResponse extends AuthenticationResponse {
    public Assertion assertion;
    
    public InternalSAMLAuthenticationResponse(AuthenticationResponse param1AuthenticationResponse, Assertion param1Assertion) {
      super(param1AuthenticationResponse);
      this.assertion = param1Assertion;
    }
  }
}
