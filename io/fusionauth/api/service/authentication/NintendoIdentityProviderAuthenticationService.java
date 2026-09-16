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
import io.fusionauth.api.service.identity.OpenIdConnectIdentityProviderHelper;
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
import io.fusionauth.domain.provider.NintendoIdentityProvider;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class NintendoIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  @Inject
  public NintendoIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    NintendoIdentityProvider nintendoIdentityProvider = (NintendoIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(nintendoIdentityProvider.debug, "Nintendo IdP Response Debug Log [" + String.valueOf(nintendoIdentityProvider.id) + "]"), nintendoIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    String str = paramIdentityProviderLoginRequest.data.get("token");
    if (str == null) {
      String str1 = paramIdentityProviderLoginRequest.data.get("code");
      String str2 = paramIdentityProviderLoginRequest.data.get("redirect_uri");
      debugger.log("An auth code [" + str1 + "] and redirect_uri [" + str2 + "] were provided. Complete the auth code grant.");
      JsonNode jsonNode1 = callAccessTokenAPI(debugger, nintendoIdentityProvider, paramIdentityProviderLoginRequest);
      str = jsonNode1.at("/access_token").asText();
    } else {
      debugger.log("An access token [" + str + "] was provided.");
    } 
    JsonNode jsonNode = callTokenInfoAPI(debugger, str);
    loginContext.identityProviderToken = str;
    loginContext.email = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, nintendoIdentityProvider.lookupEmailClaim(paramApplication.id));
    loginContext.username = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, nintendoIdentityProvider.lookupUsernameClaim(paramApplication.id));
    loginContext.identityProviderUserId = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, nintendoIdentityProvider.lookupUniqueIdClaim(paramApplication.id));
    loginContext.identityProviderDisplayName = (loginContext.email != null) ? loginContext.email : loginContext.username;
    return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJsonNode) }, ), paramUserResult -> paramJsonNode);
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.Nintendo;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    return commonValidateAuthCodeOrToken(paramTenant, paramBaseIdentityProvider, paramIdentityProviderLoginRequest, paramExternalIdentifier);
  }
  
  public OAuthService.LinkingTokenDetails verifyLinkingToken(Tenant paramTenant, Application paramApplication, UUID paramUUID, String paramString) {
    NintendoIdentityProvider nintendoIdentityProvider = getEnabledIdp(paramApplication.id, paramUUID);
    Debugger debugger = new Debugger(nintendoIdentityProvider.debug, "Nintendo IdP verify linking token Debug Log");
    OAuthService.LinkingTokenDetails linkingTokenDetails = new OAuthService.LinkingTokenDetails(nintendoIdentityProvider);
    JsonNode jsonNode = callTokenInfoAPI(debugger, paramString);
    String str1 = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, nintendoIdentityProvider.lookupEmailClaim(paramApplication.id));
    String str2 = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, nintendoIdentityProvider.lookupUsernameClaim(paramApplication.id));
    linkingTokenDetails.identityProviderDisplayName = (str1 != null) ? str1 : str2;
    linkingTokenDetails.identityProviderUserId = OpenIdConnectIdentityProviderHelper.getClaimWithPointer(jsonNode, nintendoIdentityProvider.lookupUniqueIdClaim(paramApplication.id));
    linkingTokenDetails.raw = jsonNode;
    debugger.logObjectToJSON("Completed verify linking token. Token details:\n", linkingTokenDetails)
      .done();
    return linkingTokenDetails;
  }
  
  private JsonNode callAccessTokenAPI(Debugger paramDebugger, NintendoIdentityProvider paramNintendoIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest) {
    return makeRequest(paramDebugger, "https://accounts.nintendo.com/connect/1.0.0/api/token", paramRESTClient -> paramRESTClient.basicAuthorization(paramNintendoIdentityProvider.lookupClientId(paramIdentityProviderLoginRequest.applicationId), paramNintendoIdentityProvider.lookupClientSecret(paramIdentityProviderLoginRequest.applicationId)).bodyHandler((RESTClient.BodyHandler)(new FormDataBodyHandler()).withParameter("code", paramIdentityProviderLoginRequest.data.get("code")).withParameter("grant_type", GrantType.authorization_code.name()).withParameter("redirect_uri", paramIdentityProviderLoginRequest.data.get("redirect_uri"))), ExternalAuthenticationException.Reason.NintendoToken, RESTClient.HTTPMethod.POST);
  }
  
  private JsonNode callTokenInfoAPI(Debugger paramDebugger, String paramString) {
    return makeRequest(paramDebugger, "https://api.accounts.nintendo.com/2.0.0/users/me", paramRESTClient -> paramRESTClient.authorization("Bearer " + paramString), ExternalAuthenticationException.Reason.NintendoToken, RESTClient.HTTPMethod.GET);
  }
}
