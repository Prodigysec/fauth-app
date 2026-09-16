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
import io.fusionauth.domain.provider.SonyPSNIdentityProvider;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class SonyPSNIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  @Inject
  public SonyPSNIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    SonyPSNIdentityProvider sonyPSNIdentityProvider = (SonyPSNIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(sonyPSNIdentityProvider.debug, "Sony PlayStation Network IdP Response Debug Log [" + String.valueOf(sonyPSNIdentityProvider.id) + "]"), sonyPSNIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    String str = paramIdentityProviderLoginRequest.data.get("token");
    if (str == null) {
      String str1 = paramIdentityProviderLoginRequest.data.get("code");
      String str2 = paramIdentityProviderLoginRequest.data.get("redirect_uri");
      debugger.log("An auth code [" + str1 + "] and redirect_uri [" + str2 + "]. Complete the auth code grant.");
      JsonNode jsonNode = callAccessTokenAPI(debugger, sonyPSNIdentityProvider, paramIdentityProviderLoginRequest.applicationId, str1, str2);
      str = jsonNode.at("/access_token").asText();
    } else {
      debugger.log("An access token [" + str + "] was provided.");
    } 
    JsonNode jsonNode1 = callUserInfoAPI(debugger, sonyPSNIdentityProvider, paramIdentityProviderLoginRequest.applicationId, str);
    loginContext.username = jsonNode1.at("/online_id").asText();
    loginContext.identityProviderDisplayName = loginContext.username;
    loginContext.identityProviderUserId = jsonNode1.at("/user_uuid").asText();
    loginContext.identityProviderToken = str;
    JsonNode jsonNode2 = callAccountInfoAPI(debugger, str);
    loginContext.email = jsonNode2.at("/email").asText();
    return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJsonNode) }, ), paramUserResult -> paramJsonNode);
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.SonyPSN;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    return commonValidateAuthCodeOrToken(paramTenant, paramBaseIdentityProvider, paramIdentityProviderLoginRequest, paramExternalIdentifier);
  }
  
  public OAuthService.LinkingTokenDetails verifyLinkingToken(Tenant paramTenant, Application paramApplication, UUID paramUUID, String paramString) {
    SonyPSNIdentityProvider sonyPSNIdentityProvider = getEnabledIdp(paramApplication.id, paramUUID);
    Debugger debugger = new Debugger(sonyPSNIdentityProvider.debug, "Sony IdP verify linking token Debug Log");
    OAuthService.LinkingTokenDetails linkingTokenDetails = new OAuthService.LinkingTokenDetails(sonyPSNIdentityProvider);
    JsonNode jsonNode = callUserInfoAPI(debugger, sonyPSNIdentityProvider, paramApplication.id, paramString);
    linkingTokenDetails.identityProviderDisplayName = jsonNode.at("/online_id").asText();
    linkingTokenDetails.identityProviderUserId = jsonNode.at("/user_uuid").asText();
    linkingTokenDetails.raw = jsonNode;
    debugger.logObjectToJSON("Completed verify linking token. Token details:\n", linkingTokenDetails)
      .done();
    return linkingTokenDetails;
  }
  
  private JsonNode callAccessTokenAPI(Debugger paramDebugger, SonyPSNIdentityProvider paramSonyPSNIdentityProvider, UUID paramUUID, String paramString1, String paramString2) {
    return makeRequest(paramDebugger, "https://auth.api.sonyentertainmentnetwork.com/2.0/oauth/token", paramRESTClient -> paramRESTClient.basicAuthorization(paramSonyPSNIdentityProvider.lookupClientId(paramUUID), paramSonyPSNIdentityProvider.lookupClientSecret(paramUUID)).bodyHandler((RESTClient.BodyHandler)(new FormDataBodyHandler()).withParameter("code", paramString1).withParameter("grant_type", GrantType.authorization_code.name()).withParameter("redirect_uri", paramString2)), ExternalAuthenticationException.Reason.SonyPSNToken, RESTClient.HTTPMethod.POST);
  }
  
  private JsonNode callAccountInfoAPI(Debugger paramDebugger, String paramString) {
    return makeRequest(paramDebugger, "https://vl.api.np.ac.playstation.net/vl/api/v1/s2s/users/me/info", paramRESTClient -> paramRESTClient.authorization("Bearer " + paramString), ExternalAuthenticationException.Reason.SonyPSNToken, RESTClient.HTTPMethod.GET);
  }
  
  private JsonNode callUserInfoAPI(Debugger paramDebugger, SonyPSNIdentityProvider paramSonyPSNIdentityProvider, UUID paramUUID, String paramString) {
    return makeRequest(paramDebugger, "https://auth.api.sonyentertainmentnetwork.com/2.0/oauth/token", paramRESTClient -> paramRESTClient.basicAuthorization(paramSonyPSNIdentityProvider.lookupClientId(paramUUID), paramSonyPSNIdentityProvider.lookupClientSecret(paramUUID)).urlSegment(paramString), ExternalAuthenticationException.Reason.SonyPSNToken, RESTClient.HTTPMethod.GET);
  }
}
