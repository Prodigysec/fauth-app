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
import io.fusionauth.domain.provider.TwitchIdentityProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class TwitchIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  @Inject
  public TwitchIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    TwitchIdentityProvider twitchIdentityProvider = (TwitchIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(twitchIdentityProvider.debug, "Twitch IdP Response Debug Log [" + String.valueOf(twitchIdentityProvider.id) + "]"), twitchIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    String str = paramIdentityProviderLoginRequest.data.get("token");
    if (str == null) {
      String str1 = paramIdentityProviderLoginRequest.data.get("code");
      String str2 = paramIdentityProviderLoginRequest.data.get("redirect_uri");
      debugger.log("An auth code [" + str1 + "] and redirect_uri [" + str2 + "] were provided. Complete the auth code grant.");
      JsonNode jsonNode1 = callAccessTokenAPI(debugger, twitchIdentityProvider, paramIdentityProviderLoginRequest);
      str = jsonNode1.at("/access_token").asText();
      loginContext.identityProviderToken = jsonNode1.at("/refresh_token").asText();
    } else {
      debugger.log("An access token [" + str + "] was provided.");
      loginContext.identityProviderToken = str;
    } 
    JsonNode jsonNode = callUserInfoAPI(debugger, str);
    loginContext.email = jsonNode.at("/email").asText();
    loginContext.username = jsonNode.at("/preferred_username").asText();
    loginContext.identityProviderUserId = jsonNode.at("/sub").asText();
    loginContext.identityProviderDisplayName = loginContext.username;
    return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJsonNode) }, ), paramUserResult -> paramJsonNode);
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.Twitch;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    return commonValidateAuthCodeOrToken(paramTenant, paramBaseIdentityProvider, paramIdentityProviderLoginRequest, paramExternalIdentifier);
  }
  
  private JsonNode callAccessTokenAPI(Debugger paramDebugger, TwitchIdentityProvider paramTwitchIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest) {
    return makeRequest(paramDebugger, "https://id.twitch.tv/oauth2/token", paramRESTClient -> paramRESTClient.bodyHandler((RESTClient.BodyHandler)(new FormDataBodyHandler()).withParameter("client_id", paramTwitchIdentityProvider.lookupClientId(paramIdentityProviderLoginRequest.applicationId)).withParameter("client_secret", paramTwitchIdentityProvider.lookupClientSecret(paramIdentityProviderLoginRequest.applicationId)).withParameter("code", paramIdentityProviderLoginRequest.data.get("code")).withParameter("grant_type", GrantType.authorization_code.name()).withParameter("redirect_uri", paramIdentityProviderLoginRequest.data.get("redirect_uri"))), ExternalAuthenticationException.Reason.TwitchToken, RESTClient.HTTPMethod.POST);
  }
  
  private JsonNode callUserInfoAPI(Debugger paramDebugger, String paramString) {
    return makeRequest(paramDebugger, "https://id.twitch.tv/oauth2/userinfo", paramRESTClient -> paramRESTClient.header("Authorization", "Bearer " + paramString), ExternalAuthenticationException.Reason.TwitchUserInfo, RESTClient.HTTPMethod.GET);
  }
}
