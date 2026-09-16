package io.fusionauth.api.service.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
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
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.SteamAPIMode;
import io.fusionauth.domain.provider.SteamIdentityProvider;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class SteamIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  @Inject
  public SteamIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    SteamIdentityProvider steamIdentityProvider = (SteamIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(steamIdentityProvider.debug, "Steam IdP Response Debug Log [" + String.valueOf(steamIdentityProvider.id) + "]"), steamIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    debugger.log("API mode [" + String.valueOf(steamIdentityProvider.lookupAPIMode(paramApplication.id)) + "]");
    String str1 = paramIdentityProviderLoginRequest.data.get("token");
    String str2 = paramIdentityProviderLoginRequest.data.get("appId");
    String str3 = paramIdentityProviderLoginRequest.data.get("sessionTicket");
    String str4 = getSteamId(debugger, steamIdentityProvider, paramApplication, str1, str2, str3);
    loginContext.identityProviderToken = str1;
    loginContext.identityProviderUserId = str4;
    JsonNode jsonNode = callPlayerSummaryAPI(debugger, paramIdentityProviderLoginRequest.applicationId, steamIdentityProvider, str4);
    loginContext.username = jsonNode.at("/response/players/0/personaname").asText();
    loginContext.identityProviderDisplayName = loginContext.username;
    return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJsonNode) }, ), paramUserResult -> paramJsonNode);
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.Steam;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderAuthenticationService.ValidationResult validationResult = commonValidate(paramTenant, paramIdentityProviderLoginRequest.applicationId, paramBaseIdentityProvider, paramExternalIdentifier);
    boolean bool = paramIdentityProviderLoginRequest.data.containsKey("token");
    boolean bool1 = (paramIdentityProviderLoginRequest.data.containsKey("appId") && paramIdentityProviderLoginRequest.data.containsKey("sessionTicket")) ? true : false;
    validationResult.errors.add((new Validator())
        
        .ifTrue((!bool && !bool1), paramValidator -> paramValidator.notMissing(paramIdentityProviderLoginRequest.data.get("token"), "data.token", new Object[0]))

        
        .done());
    return validationResult;
  }
  
  public OAuthService.LinkingTokenDetails verifyLinkingToken(Tenant paramTenant, Application paramApplication, UUID paramUUID, String paramString) {
    SteamIdentityProvider steamIdentityProvider = getEnabledIdp(paramApplication.id, paramUUID);
    Debugger debugger = new Debugger(steamIdentityProvider.debug, "Steam IdP verify linking token Debug Log");
    OAuthService.LinkingTokenDetails linkingTokenDetails = new OAuthService.LinkingTokenDetails(steamIdentityProvider);
    String[] arrayOfString = paramString.split(":");
    String str1 = (arrayOfString.length > 1) ? arrayOfString[0] : null;
    String str2 = (arrayOfString.length > 1) ? arrayOfString[1] : null;
    String str3 = getSteamId(debugger, steamIdentityProvider, paramApplication, paramString, str1, str2);
    linkingTokenDetails.identityProviderUserId = str3;
    JsonNode jsonNode = callPlayerSummaryAPI(debugger, paramApplication.id, steamIdentityProvider, str3);
    linkingTokenDetails.identityProviderDisplayName = jsonNode.at("/response/players/0/personaname").asText();
    linkingTokenDetails.raw = jsonNode;
    debugger.logObjectToJSON("Completed verify linking token. Token details:\n", linkingTokenDetails)
      .done();
    return linkingTokenDetails;
  }
  
  private JsonNode callAuthenticateUserTicketAPI(Debugger paramDebugger, UUID paramUUID, SteamIdentityProvider paramSteamIdentityProvider, String paramString1, String paramString2) {
    return makeRequest(paramDebugger, "https://partner.steam-api.com/ISteamUserAuth/AuthenticateUserTicket/v1/", paramRESTClient -> paramRESTClient.urlParameter("key", paramSteamIdentityProvider.lookupWebAPIKey(paramUUID)).urlParameter("appid", paramString1).urlParameter("ticket", paramString2), ExternalAuthenticationException.Reason.SteamAuthenticateUserTicket, RESTClient.HTTPMethod.GET);
  }
  
  private JsonNode callPlayerSummaryAPI(Debugger paramDebugger, UUID paramUUID, SteamIdentityProvider paramSteamIdentityProvider, String paramString) {
    return makeRequest(paramDebugger, (paramSteamIdentityProvider.lookupAPIMode(paramUUID) == SteamAPIMode.Public) ? 
        "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/" : 
        "https://partner.steam-api.com/ISteamUser/GetPlayerSummaries/v2/", paramRESTClient -> paramRESTClient.urlParameter("key", paramSteamIdentityProvider.lookupWebAPIKey(paramUUID)).urlParameter("steamids", paramString), ExternalAuthenticationException.Reason.SteamPlayerSummary, RESTClient.HTTPMethod.GET);
  }
  
  private JsonNode callTokenDetailsAPI(Debugger paramDebugger, UUID paramUUID, SteamIdentityProvider paramSteamIdentityProvider, String paramString) {
    if (paramSteamIdentityProvider.lookupAPIMode(paramUUID) == SteamAPIMode.Public)
      return makeRequest(paramDebugger, "https://api.steampowered.com/ISteamUserOAuth/GetTokenDetails/v1/", paramRESTClient -> paramRESTClient.urlParameter("access_token", paramString), ExternalAuthenticationException.Reason.SteamToken, RESTClient.HTTPMethod.GET); 
    return makeRequest(paramDebugger, "https://partner.steam-api.com/ISteamUserOAuth/GetTokenDetails/v1/", paramRESTClient -> paramRESTClient.urlParameter("access_token", paramString).urlParameter("key", paramSteamIdentityProvider.lookupWebAPIKey(paramUUID)), ExternalAuthenticationException.Reason.SteamToken, RESTClient.HTTPMethod.GET);
  }
  
  private String getSteamId(Debugger paramDebugger, SteamIdentityProvider paramSteamIdentityProvider, Application paramApplication, String paramString1, String paramString2, String paramString3) {
    if (paramSteamIdentityProvider.lookupAPIMode(paramApplication.id) == SteamAPIMode.Partner && paramString2 != null && paramString3 != null) {
      paramDebugger.logObjectToJSON("An appId and sessionTicket were provided.\n", 
          Map.of("appId", paramString2, "sessionTicket", paramString3));
      JsonNode jsonNode1 = callAuthenticateUserTicketAPI(paramDebugger, paramApplication.id, paramSteamIdentityProvider, paramString2, paramString3);
      return jsonNode1.at("/response/params/steamid").asText();
    } 
    paramDebugger.logObjectToJSON("An access token was provided.\n", 
        Map.of("token", paramString1));
    JsonNode jsonNode = callTokenDetailsAPI(paramDebugger, paramApplication.id, paramSteamIdentityProvider, paramString1);
    return jsonNode.at("/steamid").asText();
  }
}
