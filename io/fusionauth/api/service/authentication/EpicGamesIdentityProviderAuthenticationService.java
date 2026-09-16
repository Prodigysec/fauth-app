package io.fusionauth.api.service.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityProviderLinkMapper;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.domain.api.service.MutableLambdaArgument;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.identity.IdentityProviderUserService;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.oauth2.OAuthService;
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
import io.fusionauth.domain.provider.EpicGamesIdentityProvider;
import io.fusionauth.jwt.JWTUtils;
import io.fusionauth.jwt.domain.JWT;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class EpicGamesIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  @Inject
  public EpicGamesIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    EpicGamesIdentityProvider epicGamesIdentityProvider = (EpicGamesIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(epicGamesIdentityProvider.debug, "Epic Games IdP Response Debug Log [" + String.valueOf(epicGamesIdentityProvider.id) + "]"), epicGamesIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    String str = paramIdentityProviderLoginRequest.data.get("token");
    if (str == null) {
      String str1 = paramIdentityProviderLoginRequest.data.get("code");
      String str2 = paramIdentityProviderLoginRequest.data.get("redirect_uri");
      debugger.log("An auth code [" + str1 + "] and redirect_uri [" + str2 + "] were provided. Complete the auth code grant.");
      JsonNode jsonNode1 = callAccessTokenAPI(debugger, epicGamesIdentityProvider, paramIdentityProviderLoginRequest.applicationId, str1, str2);
      str = jsonNode1.at("/access_token").asText();
      loginContext.identityProviderToken = jsonNode1.at("/refresh_token").asText();
      loginContext.identityProviderUserId = jsonNode1.at("/account_id").asText();
    } else {
      debugger.log("An access token [" + str + "] was provided.");
      loginContext.identityProviderUserId = getAccountIdFromJWT(debugger, str);
    } 
    JsonNode jsonNode = callAccountInfoAPI(debugger, loginContext.identityProviderUserId, str);
    loginContext.username = jsonNode.at("/0/displayName").asText();
    loginContext.identityProviderDisplayName = loginContext.username;
    return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJsonNode) }, ), paramUserResult -> paramJsonNode);
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.EpicGames;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    return commonValidateAuthCodeOrToken(paramTenant, paramBaseIdentityProvider, paramIdentityProviderLoginRequest, paramExternalIdentifier);
  }
  
  public OAuthService.LinkingTokenDetails verifyLinkingToken(Tenant paramTenant, Application paramApplication, UUID paramUUID, String paramString) {
    EpicGamesIdentityProvider epicGamesIdentityProvider = getEnabledIdp(paramApplication.id, paramUUID);
    Debugger debugger = new Debugger(epicGamesIdentityProvider.debug, "Epic Games IdP verify linking token Debug Log");
    OAuthService.LinkingTokenDetails linkingTokenDetails = new OAuthService.LinkingTokenDetails(epicGamesIdentityProvider);
    String str = getAccountIdFromJWT(debugger, paramString);
    if (str == null)
      return linkingTokenDetails; 
    JsonNode jsonNode = callAccountInfoAPI(debugger, str, paramString);
    linkingTokenDetails.identityProviderDisplayName = jsonNode.at("/0/displayName").asText();
    linkingTokenDetails.identityProviderUserId = jsonNode.at("/0/accountId").asText();
    linkingTokenDetails.raw = jsonNode;
    debugger.logObjectToJSON("Completed verify linking token. Token details:\n", linkingTokenDetails)
      .done();
    return linkingTokenDetails;
  }
  
  private JsonNode callAccessTokenAPI(Debugger paramDebugger, EpicGamesIdentityProvider paramEpicGamesIdentityProvider, UUID paramUUID, String paramString1, String paramString2) {
    return makeRequest(paramDebugger, "https://api.epicgames.dev/epic/oauth/v1/token", paramRESTClient -> paramRESTClient.basicAuthorization(paramEpicGamesIdentityProvider.lookupClientId(paramUUID), paramEpicGamesIdentityProvider.lookupClientSecret(paramUUID)).bodyHandler((RESTClient.BodyHandler)(new FormDataBodyHandler()).withParameter("code", paramString1).withParameter("grant_type", GrantType.authorization_code.name()).withParameter("redirect_uri", paramString2).withParameter("scope", paramEpicGamesIdentityProvider.lookupScope(paramUUID)).withExcludeNullValues(true)), ExternalAuthenticationException.Reason.EpicGamesToken, RESTClient.HTTPMethod.POST);
  }
  
  private JsonNode callAccountInfoAPI(Debugger paramDebugger, String paramString1, String paramString2) {
    return makeRequest(paramDebugger, "https://api.epicgames.dev/epic/id/v1/accounts", paramRESTClient -> paramRESTClient.header("Authorization", "Bearer " + paramString1).urlParameter("accountId", paramString2), ExternalAuthenticationException.Reason.EpicGamesAccount, RESTClient.HTTPMethod.GET);
  }
  
  private String getAccountIdFromJWT(Debugger paramDebugger, String paramString) {
    String str = null;
    try {
      JWT jWT = JWTUtils.decodePayload(paramString);
      str = jWT.subject;
    } catch (Exception exception) {
      paramDebugger.log("Unable to read the provided token, perhaps it is not a valid JWT.")
        .log(exception)
        .done();
    } 
    return str;
  }
}
