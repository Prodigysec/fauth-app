package io.fusionauth.api.service.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.JSONBodyHandler;
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
import io.fusionauth.domain.provider.XboxIdentityProvider;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class XboxIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  @Inject
  public XboxIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    XboxIdentityProvider xboxIdentityProvider = (XboxIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(xboxIdentityProvider.debug, "Xbox IdP Response Debug Log [" + String.valueOf(xboxIdentityProvider.id) + "]"), xboxIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    String str = paramIdentityProviderLoginRequest.data.get("token");
    if (str == null) {
      String str1 = paramIdentityProviderLoginRequest.data.get("code");
      String str2 = paramIdentityProviderLoginRequest.data.get("redirect_uri");
      debugger.log("An auth code [" + str1 + "] and redirect_uri [" + str2 + "] were provided. Complete the auth code grant.");
      JsonNode jsonNode1 = callAccessTokenAPI(debugger, xboxIdentityProvider, paramIdentityProviderLoginRequest.applicationId, str1, str2);
      String str3 = jsonNode1.at("/access_token").asText();
      loginContext.identityProviderToken = jsonNode1.at("/refresh_token").asText();
      JsonNode jsonNode2 = callUserInfoAPI(debugger, str3);
      str = jsonNode2.at("/Token").asText();
    } else {
      debugger.log("A user token [" + str + "] was provided.");
    } 
    JsonNode jsonNode = callXboxSecurityTokenService(debugger, str);
    loginContext.username = jsonNode.at("/DisplayClaims/xui/0/gtg").asText();
    loginContext.identityProviderDisplayName = loginContext.username;
    loginContext.identityProviderUserId = jsonNode.at("/DisplayClaims/xui/0/xid").asText();
    return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJsonNode) }, ), paramUserResult -> paramJsonNode);
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.Xbox;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    return commonValidateAuthCodeOrToken(paramTenant, paramBaseIdentityProvider, paramIdentityProviderLoginRequest, paramExternalIdentifier);
  }
  
  public OAuthService.LinkingTokenDetails verifyLinkingToken(Tenant paramTenant, Application paramApplication, UUID paramUUID, String paramString) {
    XboxIdentityProvider xboxIdentityProvider = getEnabledIdp(paramApplication.id, paramUUID);
    Debugger debugger = new Debugger(xboxIdentityProvider.debug, "Xbox IdP verify linking token Debug Log");
    OAuthService.LinkingTokenDetails linkingTokenDetails = new OAuthService.LinkingTokenDetails(xboxIdentityProvider);
    JsonNode jsonNode = callXboxSecurityTokenService(debugger, paramString);
    linkingTokenDetails.identityProviderDisplayName = jsonNode.at("/DisplayClaims/xui/0/gtg").asText();
    linkingTokenDetails.identityProviderUserId = jsonNode.at("/DisplayClaims/xui/0/xid").asText();
    linkingTokenDetails.raw = jsonNode;
    debugger.logObjectToJSON("Completed verify linking token. Token details:\n", linkingTokenDetails)
      .done();
    return linkingTokenDetails;
  }
  
  private JsonNode callAccessTokenAPI(Debugger paramDebugger, XboxIdentityProvider paramXboxIdentityProvider, UUID paramUUID, String paramString1, String paramString2) {
    return makeRequest(paramDebugger, "https://login.live.com/oauth20_token.srf", paramRESTClient -> paramRESTClient.bodyHandler((RESTClient.BodyHandler)(new FormDataBodyHandler()).withParameter("client_id", paramXboxIdentityProvider.lookupClientId(paramUUID)).withParameter("client_secret", paramXboxIdentityProvider.lookupClientSecret(paramUUID)).withParameter("code", paramString1).withParameter("grant_type", GrantType.authorization_code.name()).withParameter("redirect_uri", paramString2)), ExternalAuthenticationException.Reason.XboxToken, RESTClient.HTTPMethod.POST);
  }
  
  private JsonNode callUserInfoAPI(Debugger paramDebugger, String paramString) {
    return makeRequest(paramDebugger, "https://user.auth.xboxlive.com/user/authenticate", paramRESTClient -> paramRESTClient.header("x-xbl-contract-version", "1").bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(Map.of("RelyingParty", "http://auth.xboxlive.com", "TokenType", "JWT", "Properties", Map.of("AuthMethod", "RPS", "SiteName", "user.auth.xboxlive.com", "RpsTicket", "d=" + paramString)))), ExternalAuthenticationException.Reason.XboxUserInfo, RESTClient.HTTPMethod.POST);
  }
  
  private JsonNode callXboxSecurityTokenService(Debugger paramDebugger, String paramString) {
    return makeRequest(paramDebugger, "https://xsts.auth.xboxlive.com/xsts/authorize", paramRESTClient -> paramRESTClient.header("x-xbl-contract-version", "1").bodyHandler((RESTClient.BodyHandler)new JSONBodyHandler(Map.of("RelyingParty", "http://xboxlive.com", "TokenType", "JWT", "Properties", Map.of("UserTokens", new String[] { paramString }, "SandboxId", "RETAIL")))), ExternalAuthenticationException.Reason.XboxSecurityTokenService, RESTClient.HTTPMethod.POST);
  }
}
