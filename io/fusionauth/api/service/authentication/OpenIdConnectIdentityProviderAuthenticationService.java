package io.fusionauth.api.service.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.inversoft.json.ToString;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityProviderLinkMapper;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.domain.api.service.MutableLambdaArgument;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.identity.IdentityProviderUserService;
import io.fusionauth.api.service.identity.OpenIdConnectIdentityProviderHelper;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.system.EventLogService;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.UserMetricsService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import io.fusionauth.domain.oauth2.GrantType;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderOauth2Configuration;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import io.fusionauth.jwks.JSONWebKeyParser;
import io.fusionauth.jwks.domain.JSONWebKey;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.jwt.Verifier;
import io.fusionauth.jwt.domain.Header;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.domain.KeyType;
import io.fusionauth.jwt.ec.ECVerifier;
import io.fusionauth.jwt.hmac.HMACVerifier;
import io.fusionauth.jwt.rsa.RSAVerifier;
import java.security.PublicKey;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class OpenIdConnectIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  @Inject
  public OpenIdConnectIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    OpenIdConnectIdentityProvider openIdConnectIdentityProvider = (OpenIdConnectIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(openIdConnectIdentityProvider.debug, "OpenID Connect IdP Response Debug Log for [" + openIdConnectIdentityProvider.name + "] [" + String.valueOf(openIdConnectIdentityProvider.id) + "]"), openIdConnectIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    FormDataBodyHandler formDataBodyHandler = (new FormDataBodyHandler()).withParameter("code", paramIdentityProviderLoginRequest.data.get("code")).withParameter("grant_type", GrantType.authorization_code.name()).withParameter("redirect_uri", paramIdentityProviderLoginRequest.data.get("redirect_uri"));
    String str1 = openIdConnectIdentityProvider.lookupClientId(paramIdentityProviderLoginRequest.applicationId);
    String str2 = openIdConnectIdentityProvider.lookupClientSecret(paramIdentityProviderLoginRequest.applicationId);
    if (str2 == null || openIdConnectIdentityProvider.oauth2.clientAuthenticationMethod == IdentityProviderOauth2Configuration.ClientAuthenticationMethod.client_secret_post || openIdConnectIdentityProvider.oauth2.clientAuthenticationMethod == IdentityProviderOauth2Configuration.ClientAuthenticationMethod.none)
      formDataBodyHandler.withParameter("client_id", str1); 
    String str3 = paramIdentityProviderLoginRequest.data.get("code_verifier");
    if (str3 != null)
      formDataBodyHandler.withParameter("code_verifier", str3); 
    if (str2 != null && openIdConnectIdentityProvider.oauth2.clientAuthenticationMethod == IdentityProviderOauth2Configuration.ClientAuthenticationMethod.client_secret_post)
      formDataBodyHandler.withParameter("client_secret", str2); 
    String str4 = openIdConnectIdentityProvider.oauth2.token_endpoint.toString();
    debugger.log("Call the configured Token endpoint [" + str4 + "]");
    RESTClient rESTClient = new RESTClient(JsonNode.class, JsonNode.class);
    if (str2 != null && openIdConnectIdentityProvider.oauth2.clientAuthenticationMethod == IdentityProviderOauth2Configuration.ClientAuthenticationMethod.client_secret_basic)
      rESTClient.basicAuthorization(str1, str2); 
    ClientResponse<?, ?> clientResponse1 = rESTClient.header("Accept", "application/json").url(str4).readTimeout(10000).proxy((ProxyInfo)this.proxyInfoSupplier.get()).bodyHandler((RESTClient.BodyHandler)formDataBodyHandler).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class)).errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class)).post().go();
    debugger.log("Endpoint returned status code [" + clientResponse1.status + "]");
    if (!clientResponse1.wasSuccessful()) {
      debugger.handleError(clientResponse1, "Request to the [" + str4 + "] endpoint failed.");
      if (loginContext.connectionTestId != null)
        loginContext.connectionTestId.data.addTraceStep("OpenID Connect token", false, String.format("The request to the [%s] token endpoint has failed. Unable to complete login.", new Object[] { str4 })); 
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.OpenIDConnectToken);
    } 
    debugger.logObjectToJSON("Access Token Response:\n", clientResponse1.successResponse);
    JsonNode jsonNode1 = (JsonNode)clientResponse1.successResponse;
    if (!jsonNode1.has("access_token")) {
      debugger.log("Access Token response was successful, however it did not contain the required access_token property.")
        .done();
      if (loginContext.connectionTestId != null)
        loginContext.connectionTestId.data.addTraceStep("OpenID Connect token", false, "The OIDC token endpoint did not return an access token. Unable to complete login."); 
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.OpenIDConnectToken);
    } 
    String str5 = jsonNode1.get("access_token").asText();
    String str6 = openIdConnectIdentityProvider.oauth2.userinfo_endpoint.toString();
    debugger.log("Call the configured Userinfo endpoint [" + str6 + "]");
    ClientResponse<?, ?> clientResponse2 = (new RESTClient(JsonNode.class, String.class)).url(str6).readTimeout(10000).proxy((ProxyInfo)this.proxyInfoSupplier.get()).header("Authorization", "Bearer " + str5).successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class)).errorResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler()).get().go();
    debugger.log("Endpoint returned status code [" + clientResponse2.status + "]");
    if (!clientResponse2.wasSuccessful()) {
      debugger.handleError(clientResponse2, "Request to the [" + str6 + "] endpoint failed.");
      if (loginContext.connectionTestId != null)
        loginContext.connectionTestId.data.addTraceStep("OpenID Connect userinfo", false, String.format("The request to the [%s] userinfo endpoint has failed. Unable to complete login.", new Object[] { str6 })); 
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.OpenIDConnectUserinfo);
    } 
    JsonNode jsonNode2 = (JsonNode)clientResponse2.successResponse;
    debugger.log("Build a new user object from the returned Userinfo response:\n" + ToString.toString(jsonNode2));
    loginContext.email = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode2, openIdConnectIdentityProvider.oauth2.emailClaim);
    loginContext.email_verified = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode2, openIdConnectIdentityProvider.oauth2.emailVerifiedClaim);
    loginContext.username = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode2, openIdConnectIdentityProvider.oauth2.usernameClaim);
    loginContext.identityProviderUserId = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode2, openIdConnectIdentityProvider.oauth2.uniqueIdClaim);
    loginContext.identityProviderDisplayName = (loginContext.email != null) ? loginContext.email : loginContext.username;
    loginContext.identityProviderToken = jsonNode1.at("/refresh_token").asText();
    Function<BaseIdentityProviderAuthenticationService.UserResult, LambdaArgument[]> function = paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJsonNode), new ImmutableLambdaArgument(null), new ImmutableLambdaArgument(Map.of("access_token", paramString)) });
    JsonNode jsonNode3 = null;
    if (jsonNode1.has("id_token")) {
      String str7 = jsonNode1.get("id_token").asText();
      Header header = JWTUtils.decodeHeader(str7);
      String str8 = header.getString("kid");
      Verifier verifier = getIdTokenVerifier(openIdConnectIdentityProvider, str2, debugger, str8);
      if (verifier != null)
        if (verifier.canVerify(header.algorithm)) {
          debugger.log("Decode the [id_token].");
          JWT jWT = null;
          try {
            jWT = JWT.getDecoder().decode(str7, new Verifier[] { verifier });
          } catch (Exception exception) {
            debugger.log("Failed to validate the [id_token] so the decoded value will not be present in the lambda function.")
              .log(exception);
          } 
          if (jWT != null) {
            JsonNode jsonNode = OpenIdConnectIdentityProviderHelper.jwtToJsonNode(jWT);
            jsonNode3 = jsonNode;
            if (loginContext.email == null)
              loginContext.email = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, openIdConnectIdentityProvider.oauth2.emailClaim); 
            if (loginContext.email_verified == null)
              loginContext.email_verified = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, openIdConnectIdentityProvider.oauth2.emailVerifiedClaim); 
            if (loginContext.username == null)
              loginContext.username = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, openIdConnectIdentityProvider.oauth2.usernameClaim); 
            loginContext.identityProviderDisplayName = (loginContext.email != null) ? loginContext.email : loginContext.username;
            if (loginContext.identityProviderUserId == null)
              loginContext.identityProviderUserId = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, openIdConnectIdentityProvider.oauth2.uniqueIdClaim); 
            function = (paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJsonNode1), new ImmutableLambdaArgument(paramJsonNode2), new ImmutableLambdaArgument(Map.of("access_token", paramString1, "id_token", paramString2)) }));
          } 
        } else {
          debugger.log("Unable to verify the [id_token]. A JWT verifier was built using using the [kid] value of [" + str8 + "] but the verifier cannot verify token signed using [" + String.valueOf(header.algorithm) + "].");
        }  
    } 
    ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
    objectNode.set("jwt", jsonNode2);
    objectNode.set("id_token", (jsonNode3 != null) ? jsonNode3 : (JsonNode)NullNode.instance);
    return completeLogin(loginContext, null, function, paramUserResult -> paramObjectNode);
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.OPENID_CONNECT;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderAuthenticationService.ValidationResult validationResult = commonValidate(paramTenant, paramIdentityProviderLoginRequest.applicationId, paramBaseIdentityProvider, paramExternalIdentifier);
    validationResult.errors.add((new Validator())
        .notMissing(paramIdentityProviderLoginRequest.data.get("code"), "data.code", new Object[0])
        .notMissing(paramIdentityProviderLoginRequest.data.get("redirect_uri"), "data.redirect_uri", new Object[0])
        .done());
    return validationResult;
  }
  
  private Verifier getIdTokenVerifier(OpenIdConnectIdentityProvider paramOpenIdConnectIdentityProvider, String paramString1, Debugger paramDebugger, String paramString2) {
    try {
      Map map = (Map)paramOpenIdConnectIdentityProvider.data.get(OpenIdConnectIdentityProviderHelper.JWKSDataKey);
      if (map != null) {
        JSONWebKey jSONWebKey = (JSONWebKey)map.get(paramString2);
        if (jSONWebKey != null) {
          PublicKey publicKey = (new JSONWebKeyParser()).parse(jSONWebKey);
          return (jSONWebKey.kty == KeyType.EC) ? 
            (Verifier)ECVerifier.newVerifier(publicKey) : 
            (Verifier)RSAVerifier.newVerifier(publicKey);
        } 
        paramDebugger.log("Unable to resolve a public key from JWKS using kid [" + paramString2 + "].");
      } else {
        paramDebugger.log("No JSON Web Keys are available to attempt to verify the [id_token].");
      } 
      if (paramString1 != null) {
        paramDebugger.log("Attempt to verify the [id_token] using the client secret.");
        return (Verifier)HMACVerifier.newVerifier(paramString1);
      } 
    } catch (Exception exception) {
      paramDebugger.log("Unable to construct a JWT verifier for the [id_token].")
        .log(exception);
    } 
    return null;
  }
}
