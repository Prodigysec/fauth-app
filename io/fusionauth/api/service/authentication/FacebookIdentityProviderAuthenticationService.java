package io.fusionauth.api.service.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
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
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.FacebookIdentityProvider;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class FacebookIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  @Inject
  public FacebookIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    FacebookIdentityProvider facebookIdentityProvider = (FacebookIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(facebookIdentityProvider.debug, "Facebook IdP Response Debug Log [" + String.valueOf(facebookIdentityProvider.id) + "]"), facebookIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    JsonNode jsonNode1 = callAccessTokenAPI(debugger, facebookIdentityProvider, paramIdentityProviderLoginRequest);
    String str = jsonNode1.at("/access_token").asText();
    JsonNode jsonNode2 = callMeAPI(debugger, facebookIdentityProvider, paramIdentityProviderLoginRequest, str);
    JsonNode jsonNode3 = jsonNode2.at("/picture");
    if (jsonNode3.isMissingNode()) {
      jsonNode3 = callPictureEndpoint(debugger, str);
      if (!jsonNode3.isMissingNode()) {
        ObjectNode objectNode = (ObjectNode)jsonNode2;
        objectNode.set("picture", jsonNode3);
      } 
    } 
    loginContext.email = jsonNode2.at("/email").asText();
    loginContext.identityProviderToken = str;
    loginContext.identityProviderUserId = jsonNode2.at("/id").asText();
    loginContext.identityProviderDisplayName = loginContext.email;
    return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJsonNode) }, ), paramUserResult -> paramJsonNode);
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.FACEBOOK;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    return commonValidateAuthCodeOrToken(paramTenant, paramBaseIdentityProvider, paramIdentityProviderLoginRequest, paramExternalIdentifier);
  }
  
  private JsonNode callAccessTokenAPI(Debugger paramDebugger, FacebookIdentityProvider paramFacebookIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest) {
    Map map;
    String str1 = paramFacebookIdentityProvider.lookupAppId(paramIdentityProviderLoginRequest.applicationId);
    String str2 = paramFacebookIdentityProvider.lookupClientSecret(paramIdentityProviderLoginRequest.applicationId);
    if (paramIdentityProviderLoginRequest.data.containsKey("code")) {
      map = Map.of("client_id", str1, "client_secret", str2, "code", paramIdentityProviderLoginRequest.data
          
          .get("code"), "redirect_uri", paramIdentityProviderLoginRequest.data
          .get("redirect_uri"));
      paramDebugger.log("Using code and redirect_uri with login method: " + String.valueOf(paramFacebookIdentityProvider.lookupLoginMethod(str1)));
    } else {
      map = Map.of("client_id", str1, "client_secret", str2, "grant_type", "fb_exchange_token", "fb_exchange_token", paramIdentityProviderLoginRequest.data

          
          .get("token"));
      paramDebugger.log("Using access token with login method: " + String.valueOf(paramFacebookIdentityProvider.lookupLoginMethod(str1)));
    } 
    return makeRequest(paramDebugger, "https://graph.facebook.com/v3.1/oauth/access_token", paramRESTClient -> {
          Objects.requireNonNull(paramRESTClient);
          paramMap.forEach(paramRESTClient::urlParameter);
        }ExternalAuthenticationException.Reason.FacebookAccessToken, RESTClient.HTTPMethod.GET);
  }
  
  private JsonNode callMeAPI(Debugger paramDebugger, FacebookIdentityProvider paramFacebookIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, String paramString) {
    Map map = Map.of("fields", Objects.requireNonNullElse(paramFacebookIdentityProvider.lookupFields(paramIdentityProviderLoginRequest.applicationId), "email"), "access_token", paramString);
    return makeRequest(paramDebugger, "https://graph.facebook.com/v3.1/me", paramRESTClient -> {
          Objects.requireNonNull(paramRESTClient);
          paramMap.forEach(paramRESTClient::urlParameter);
        }ExternalAuthenticationException.Reason.FacebookMe, RESTClient.HTTPMethod.GET);
  }
  
  private JsonNode callPictureEndpoint(Debugger paramDebugger, String paramString) {
    Map map = Map.of("width", "9999", "access_token", paramString, "redirect", "false");
    return makeRequest(paramDebugger, "https://graph.facebook.com/v3.1/me/picture", paramRESTClient -> {
          Objects.requireNonNull(paramRESTClient);
          paramMap.forEach(paramRESTClient::urlParameter);
        }ExternalAuthenticationException.Reason.FacebookMePicture, RESTClient.HTTPMethod.GET);
  }
}
