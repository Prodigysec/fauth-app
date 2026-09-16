package io.fusionauth.api.service.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.inject.Inject;
import com.inversoft.rest.FormDataBodyHandler;
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
import io.fusionauth.domain.provider.LinkedInIdentityProvider;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class LinkedInIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  @Inject
  public LinkedInIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    JsonNode jsonNode2;
    LinkedInIdentityProvider linkedInIdentityProvider = (LinkedInIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(linkedInIdentityProvider.debug, "LinkedIn IdP Response Debug Log [" + String.valueOf(linkedInIdentityProvider.id) + "]"), linkedInIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    JsonNode jsonNode1 = callAccessTokenAPI(debugger, linkedInIdentityProvider, paramIdentityProviderLoginRequest);
    String str = jsonNode1.at("/access_token").asText();
    Set set = Set.of((Object[])linkedInIdentityProvider.scope.split(" "));
    if (set.contains("r_emailaddress") || set.contains("r_liteprofile") || set.contains("r_basicprofile")) {
      jsonNode2 = callMeAPI(debugger, str);
      if (set.contains("r_emailaddress")) {
        JsonNode jsonNode = callEmailAPI(debugger, str);
        loginContext.email = jsonNode.at("/elements/0/handle~0/emailAddress").asText();
        ((ObjectNode)jsonNode2).set("emailAddress", (JsonNode)TextNode.valueOf(loginContext.email));
      } 
      loginContext.identityProviderUserId = jsonNode2.at("/id").asText();
      loginContext.username = jsonNode2.at("/vanityName").asText();
    } else {
      jsonNode2 = callUserInfoAPI(debugger, str);
      loginContext.identityProviderUserId = jsonNode2.get("sub").asText();
      if (jsonNode2.has("email")) {
        loginContext.email = jsonNode2.get("email").asText();
        loginContext.email_verified = jsonNode2.get("email_verified").asText();
      } 
    } 
    loginContext.identityProviderToken = str;
    loginContext.identityProviderDisplayName = (loginContext.email != null) ? loginContext.email : loginContext.username;
    return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJsonNode) }, ), paramUserResult -> paramJsonNode);
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.LINKEDIN;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderAuthenticationService.ValidationResult validationResult = commonValidate(paramTenant, paramIdentityProviderLoginRequest.applicationId, paramBaseIdentityProvider, paramExternalIdentifier);
    validationResult.errors.add((new Validator())
        .notMissing(paramIdentityProviderLoginRequest.data.get("code"), "data.code", new Object[0])
        .notMissing(paramIdentityProviderLoginRequest.data.get("redirect_uri"), "data.redirect_uri", new Object[0])
        .done());
    return validationResult;
  }
  
  private JsonNode callAccessTokenAPI(Debugger paramDebugger, LinkedInIdentityProvider paramLinkedInIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest) {
    return makeRequest(paramDebugger, "https://www.linkedin.com/oauth/v2/accessToken", paramRESTClient -> paramRESTClient.bodyHandler((RESTClient.BodyHandler)(new FormDataBodyHandler()).withParameter("client_id", paramLinkedInIdentityProvider.lookupClientId(paramIdentityProviderLoginRequest.applicationId)).withParameter("client_secret", paramLinkedInIdentityProvider.lookupClientSecret(paramIdentityProviderLoginRequest.applicationId)).withParameter("code", paramIdentityProviderLoginRequest.data.get("code")).withParameter("grant_type", GrantType.authorization_code.name()).withParameter("redirect_uri", paramIdentityProviderLoginRequest.data.get("redirect_uri"))), ExternalAuthenticationException.Reason.LinkedInToken, RESTClient.HTTPMethod.POST);
  }
  
  private JsonNode callEmailAPI(Debugger paramDebugger, String paramString) {
    return makeRequest(paramDebugger, "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))", paramRESTClient -> paramRESTClient.authorization("Bearer " + paramString), ExternalAuthenticationException.Reason.LinkedInEmail, RESTClient.HTTPMethod.GET);
  }
  
  private JsonNode callMeAPI(Debugger paramDebugger, String paramString) {
    return makeRequest(paramDebugger, "https://api.linkedin.com/v2/me?projection=(id,firstName,lastName,localizedFirstName,localizedLastName,profilePicture(displayImage~:playableStreams))", paramRESTClient -> paramRESTClient.authorization("Bearer " + paramString), ExternalAuthenticationException.Reason.LinkedInMe, RESTClient.HTTPMethod.GET);
  }
  
  private JsonNode callUserInfoAPI(Debugger paramDebugger, String paramString) {
    return makeRequest(paramDebugger, "https://api.linkedin.com/v2/userinfo", paramRESTClient -> paramRESTClient.authorization("Bearer " + paramString), ExternalAuthenticationException.Reason.LinkedInUserInfo, RESTClient.HTTPMethod.GET);
  }
}
