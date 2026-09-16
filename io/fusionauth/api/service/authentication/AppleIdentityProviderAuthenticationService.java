package io.fusionauth.api.service.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityProviderLinkMapper;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.domain.api.service.MutableLambdaArgument;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.cache.JSONWebKeysCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.identity.IdentityProviderUserService;
import io.fusionauth.api.service.identity.OpenIdConnectIdentityProviderHelper;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.system.EventLogService;
import io.fusionauth.api.service.system.KeyReaderService;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.UserMetricsService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.provider.AppleIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.jwt.OpenIDConnect;
import io.fusionauth.jwt.Signer;
import io.fusionauth.jwt.domain.Header;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.ec.ECSigner;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class AppleIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  private final JSONWebKeysCache jsonWebKeys;
  
  private final KeyReaderService keyReader;
  
  private final ObjectMapper objectMapper;
  
  @Inject
  public AppleIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, JSONWebKeysCache paramJSONWebKeysCache, KeyReaderService paramKeyReaderService, LambdaInvocationService paramLambdaInvocationService, ObjectMapper paramObjectMapper, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
    this.jsonWebKeys = paramJSONWebKeysCache;
    this.keyReader = paramKeyReaderService;
    this.objectMapper = paramObjectMapper;
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    AppleIdentityProvider appleIdentityProvider = (AppleIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(appleIdentityProvider.debug, "Apple IdP Response Debug Log [" + String.valueOf(appleIdentityProvider.id) + "]"), appleIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    boolean bool = Boolean.parseBoolean(paramIdentityProviderLoginRequest.data.getOrDefault("isNativeApp", "false"));
    debugger.log("isNativeApp [" + bool + "].");
    String str1 = paramIdentityProviderLoginRequest.data.get("code");
    String str2 = paramIdentityProviderLoginRequest.data.get("id_token");
    debugger.log("Validate the provided [id_token] value [" + str2 + "]");
    JWT jWT = validateIdToken(debugger, appleIdentityProvider, paramIdentityProviderLoginRequest.applicationId, str1, str2, bool);
    debugger.log("Generate the [client_secret] used to call the configured Token endpoint.");
    String str3 = buildClientSecret(paramIdentityProviderLoginRequest.applicationId, appleIdentityProvider, bool);
    String str4 = bool ? appleIdentityProvider.lookupBundleId(paramIdentityProviderLoginRequest.applicationId) : appleIdentityProvider.lookupServicesId(paramIdentityProviderLoginRequest.applicationId);
    FormDataBodyHandler formDataBodyHandler = (new FormDataBodyHandler()).withParameter("client_id", str4).withParameter("client_secret", str3).withParameter("code", str1).withParameter("grant_type", GrantType.authorization_code.name());
    if (!bool || paramIdentityProviderLoginRequest.data.containsKey("redirect_uri"))
      formDataBodyHandler.withParameter("redirect_uri", paramIdentityProviderLoginRequest.data.get("redirect_uri")); 
    String str5 = String.valueOf(AppleIdentityProvider.ISSUER) + "/auth/token";
    debugger.log("Call the configured Token endpoint [" + str5 + "] with the the following [client_secret] value:\n" + str3);
    ClientResponse<?, ?> clientResponse = (new RESTClient(JsonNode.class, JsonNode.class)).url(str5).readTimeout(10000).bodyHandler((RESTClient.BodyHandler)formDataBodyHandler).proxy((ProxyInfo)this.proxyInfoSupplier.get()).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class)).errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class)).post().go();
    debugger.log("Endpoint returned status code [" + clientResponse.status + "]");
    if (!clientResponse.wasSuccessful()) {
      debugger.handleError(clientResponse, "Request to the [" + str5 + "] endpoint failed.");
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.AppleTokenEndpoint);
    } 
    debugger.logObjectToJSON("Access Token Response:\n", clientResponse.successResponse);
    if (paramIdentityProviderLoginRequest.data.containsKey("appleUser")) {
      String str = paramIdentityProviderLoginRequest.data.get("appleUser");
      try {
        JsonNode jsonNode = (JsonNode)this.objectMapper.readerFor(JsonNode.class).readValue(str);
        debugger.logObjectToJSON("Apple User:\n", jsonNode);
        jWT.addClaim("user", this.objectMapper.readerFor(JsonNode.class).readValue(jsonNode));
      } catch (Exception exception) {
        debugger.log("Failed to deserialize the Apple user object. Incoming value:\n" + str)
          .log(exception)
          .done();
        throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.AppleUserObject);
      } 
    } 
    debugger.log("Build a new user object from the claims collected from the [id_token] and the Token endpoint response.");
    loginContext.email = jWT.getString("email");
    loginContext.identityProviderToken = ((JsonNode)clientResponse.successResponse).at("/refresh_token").asText();
    loginContext.identityProviderUserId = jWT.subject;
    loginContext.identityProviderDisplayName = loginContext.email;
    return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJWT) }, ), paramUserResult -> OpenIdConnectIdentityProviderHelper.jwtToJsonNode(paramJWT));
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.APPLE;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderAuthenticationService.ValidationResult validationResult = commonValidate(paramTenant, paramIdentityProviderLoginRequest.applicationId, paramBaseIdentityProvider, paramExternalIdentifier);
    AppleIdentityProvider appleIdentityProvider = (AppleIdentityProvider)validationResult.identityProvider;
    boolean bool = Boolean.parseBoolean(paramIdentityProviderLoginRequest.data.getOrDefault("isNativeApp", "false"));
    validationResult.errors.add((new Validator())
        .notMissing(paramIdentityProviderLoginRequest.data.get("code"), "data.code", new Object[0])
        .notMissing(paramIdentityProviderLoginRequest.data.get("id_token"), "data.id_token", new Object[0])
        
        .ifFalse(bool, paramValidator -> paramValidator.notMissing(paramIdentityProviderLoginRequest.data.get("redirect_uri"), "data.redirect_uri", new Object[0]))
        .done());
    if (bool && StringTools.isTrimmedEmpty(appleIdentityProvider.bundleId)) {
      validationResult.errors.addGeneralError("[AppleConfigurationError]", null, new Object[] { "bundleId", "native app" });
    } else if (!bool && StringTools.isTrimmedEmpty(appleIdentityProvider.servicesId)) {
      validationResult.errors.addGeneralError("[AppleConfigurationError]", null, new Object[] { "servicesId", "web" });
    } 
    return validationResult;
  }
  
  private String buildClientSecret(UUID paramUUID, AppleIdentityProvider paramAppleIdentityProvider, boolean paramBoolean) {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    JWT jWT = (new JWT()).setIssuer(paramAppleIdentityProvider.lookupTeamId(paramUUID)).setIssuedAt(zonedDateTime).setExpiration(zonedDateTime.plusMinutes(30L)).setAudience(AppleIdentityProvider.ISSUER).setSubject(paramBoolean ? paramAppleIdentityProvider.lookupBundleId(paramUUID) : paramAppleIdentityProvider.lookupServicesId(paramUUID));
    Key key = this.keyReader.retrieveById(paramAppleIdentityProvider.lookupKeyId(paramUUID));
    ECSigner eCSigner = ECSigner.newSHA256Signer(key.privateKey, key.kid);
    return JWT.getEncoder().encode(jWT, (Signer)eCSigner);
  }
  
  private JWT validateIdToken(Debugger paramDebugger, AppleIdentityProvider paramAppleIdentityProvider, UUID paramUUID, String paramString1, String paramString2, boolean paramBoolean) {
    JWT jWT;
    try {
      paramDebugger.log("Decode the [id_token].");
      jWT = JWT.getDecoder().decode(paramString2, this.jsonWebKeys.getVerifiers(paramAppleIdentityProvider.issuer()));
    } catch (Exception exception) {
      paramDebugger.log("Failed to validate the [id_token] returned from Apple.")
        .log(exception)
        .done();
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.AppleIdToken);
    } 
    if (jWT == null) {
      paramDebugger.log("Failed to validate the [id_token] returned from Apple.")
        .done();
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.AppleIdToken);
    } 
    paramDebugger.log("Assert the [iss] claim is equal to [" + String.valueOf(AppleIdentityProvider.ISSUER) + "].");
    if (jWT.issuer == null || !jWT.issuer.equals(AppleIdentityProvider.ISSUER.toString())) {
      paramDebugger.log("The [id_token] issuer (iss) is un-expected. Expected [" + String.valueOf(AppleIdentityProvider.ISSUER) + "] and found [" + jWT.issuer + "]")
        .done();
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.AppleIdToken);
    } 
    String str1 = paramBoolean ? paramAppleIdentityProvider.lookupBundleId(paramUUID) : paramAppleIdentityProvider.lookupServicesId(paramUUID);
    paramDebugger.log("Assert the [aud] claim is equal to [" + str1 + "].");
    if (jWT.audience == null || !jWT.audience.equals(str1)) {
      paramDebugger.log("The [id_token] audience (aud) is un-expected. Expected [" + str1 + "] and found [" + String.valueOf(jWT.audience) + "]")
        .done();
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.AppleIdToken);
    } 
    paramDebugger.log("Calculate the [c_hash] to ensure the integrity of the provided [code] value [" + paramString1 + "].");
    Header header = JWTUtils.decodeHeader(paramString2);
    String str2 = jWT.getString("c_hash");
    String str3 = OpenIDConnect.c_hash(paramString1, header.algorithm);
    if (!str3.equals(str2)) {
      paramDebugger.log("The [id_token] integrity check failed. Expected a [c_hash] of [" + str2 + "] and found [" + str3 + "].")
        .done();
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.AppleIdToken);
    } 
    return jWT;
  }
}
