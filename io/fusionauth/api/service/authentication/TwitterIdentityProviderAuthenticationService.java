package io.fusionauth.api.service.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.FormDataBodyHandler;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import com.inversoft.rest.TextResponseHandler;
import com.inversoft.util.SecurityTools;
import com.inversoft.util.StringTools;
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
import io.fusionauth.api.service.oauth1.OAuth1AuthorizationHeaderBuilder;
import io.fusionauth.api.service.system.EventLogService;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.UserMetricsService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.TwitterIdentityProvider;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class TwitterIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService implements OAuth1IdentityProviderAuthenticationService {
  @Inject
  public TwitterIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExpressionEvaluator paramExpressionEvaluator, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    String str1, str2, str3;
    TwitterIdentityProvider twitterIdentityProvider = (TwitterIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(twitterIdentityProvider.debug, "Twitter IdP Response Debug Log [" + String.valueOf(twitterIdentityProvider.id) + "]"), twitterIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    if (paramIdentityProviderLoginRequest.data.containsKey("oauth_verifier")) {
      str1 = (String)(requestAccessToken(debugger, paramIdentityProviderLoginRequest, twitterIdentityProvider)).successResponse;
      String[] arrayOfString = str1.split("&");
      str2 = arrayOfString[0].substring("oauth_token=".length());
      str3 = arrayOfString[1].substring("oauth_token_secret=".length());
    } else {
      debugger.log("The [oauth_verifier] was omitted from the login request. Assume the [oauth_token] and [oauth_token_secret] are an access token instead of a request token. Skip the request to [/oauth/access_token]. If you have not already exchanged the [oauth_token] and [oauth_token_secret] for an access token then this request may fail. If this occurs you should retry the request by providing the [oauth_verifier].");
      str2 = paramIdentityProviderLoginRequest.data.get("oauth_token");
      str3 = paramIdentityProviderLoginRequest.data.get("oauth_token_secret");
      str1 = "oauth_token=" + str2 + "&" + "oauth_token_secret=" + str3;
    } 
    String str4 = "https://api.twitter.com/1.1/account/verify_credentials.json";
    debugger.log("Call the [" + str4 + "] endpoint.");
    ClientResponse<?, ?> clientResponse = (new RESTClient(JsonNode.class, String.class)).url(str4).header("Authorization", (new OAuth1AuthorizationHeaderBuilder()).withMethod("GET").withURL(str4).withConsumerSecret(twitterIdentityProvider.lookupConsumerSecret(paramIdentityProviderLoginRequest.applicationId)).withTokenSecret(str3).withParameter("include_email", "true").withParameter("oauth_consumer_key", twitterIdentityProvider.lookupConsumerKey(paramIdentityProviderLoginRequest.applicationId)).withParameter("oauth_token", str2).withParameter("oauth_nonce", SecurityTools.secureRandom()).build()).readTimeout(10000).proxy((ProxyInfo)this.proxyInfoSupplier.get()).urlParameter("include_email", "true").successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class)).errorResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler()).get().go();
    debugger.log("Endpoint returned status code [" + clientResponse.status + "]");
    if (clientResponse.status != 200) {
      debugger.handleError(clientResponse, "Request to the [" + str4 + "] endpoint failed.");
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.TwitterVerifyCredentials);
    } 
    debugger.logObjectToJSON("Build a new user object from the returned identity claims. Twitter response:\n\n", clientResponse.successResponse);
    JsonNode jsonNode = (JsonNode)clientResponse.successResponse;
    loginContext.email = jsonNode.at("/email").asText();
    loginContext.username = jsonNode.at("/screen_name").asText();
    loginContext.identityProviderToken = str1;
    loginContext.identityProviderUserId = jsonNode.at("/id").asText();
    loginContext.identityProviderDisplayName = jsonNode.at("/screen_name").asText();
    return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramJsonNode) }, ), paramUserResult -> paramJsonNode);
  }
  
  public String authenticateURI() {
    return "https://api.twitter.com/oauth/authenticate";
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.TWITTER;
  }
  
  public OAuth1IdentityProviderAuthenticationService.RequestToken requestRequestToken(String paramString1, String paramString2, UUID paramUUID) {
    UUID uUID = StringTools.parseUUID(paramString1);
    TwitterIdentityProvider twitterIdentityProvider = getEnabledIdp(uUID, paramUUID);
    Debugger debugger = new Debugger(twitterIdentityProvider.debug, "Twitter request Token Debug Log [" + String.valueOf(twitterIdentityProvider.id) + "]");
    String str1 = "https://api.twitter.com/oauth/request_token";
    debugger.log("Call the [" + str1 + "] endpoint.");
    ClientResponse<?, ?> clientResponse = (new RESTClient(String.class, String.class)).url(str1).header("Authorization", (new OAuth1AuthorizationHeaderBuilder()).withMethod("POST").withURL(str1).withConsumerSecret(twitterIdentityProvider.lookupConsumerSecret(uUID)).withParameter("oauth_callback", paramString2).withParameter("oauth_consumer_key", twitterIdentityProvider.lookupConsumerKey(uUID)).withParameter("oauth_nonce", SecurityTools.secureRandom()).build()).readTimeout(10000).proxy((ProxyInfo)this.proxyInfoSupplier.get()).successResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler()).errorResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler()).post().go();
    debugger.log("Endpoint returned status code [" + clientResponse.status + "]");
    if (clientResponse.status != 200) {
      debugger.handleError(clientResponse, "Request to the [" + str1 + "] endpoint failed.");
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.TwitterRequestToken);
    } 
    String[] arrayOfString = ((String)clientResponse.successResponse).split("&");
    String str2 = arrayOfString[0].substring("oauth_token=".length());
    String str3 = arrayOfString[1].substring("oauth_token_secret=".length());
    String str4 = arrayOfString[2].substring("oauth_callback_confirmed=".length());
    if (!Boolean.parseBoolean(str4)) {
      debugger.log("The response was not successful, see the error event log.")
        .done();
      this.eventLogService.create(new EventLog(EventLogType.Error, "The requested callback '[" + paramString2 + "] is not confirmed. Twitter responded with oauth_callback_confirmed=false"));
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.TwitterCallbackUnconfirmed);
    } 
    return new OAuth1IdentityProviderAuthenticationService.RequestToken(str2, str3);
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderAuthenticationService.ValidationResult validationResult = commonValidate(paramTenant, paramIdentityProviderLoginRequest.applicationId, paramBaseIdentityProvider, paramExternalIdentifier);
    validationResult.errors.add((new Validator())
        .notMissing(paramIdentityProviderLoginRequest.data.get("oauth_token"), "data.oauth_token", new Object[0])
        .notMissing(paramIdentityProviderLoginRequest.data.get("oauth_token_secret"), "data.oauth_token_secret", new Object[0])
        .done());
    return validationResult;
  }
  
  private ClientResponse<String, String> requestAccessToken(Debugger paramDebugger, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, TwitterIdentityProvider paramTwitterIdentityProvider) {
    String str = "https://api.twitter.com/oauth/access_token";
    paramDebugger.log("Call the [" + str + "] endpoint.");
    ClientResponse<?, ?> clientResponse = (new RESTClient(String.class, String.class)).url(str).header("Authorization", (new OAuth1AuthorizationHeaderBuilder()).withMethod("POST").withURL(str).withConsumerSecret(paramTwitterIdentityProvider.lookupConsumerSecret(paramIdentityProviderLoginRequest.applicationId)).withTokenSecret(paramIdentityProviderLoginRequest.data.get("oauth_token_secret")).withParameter("oauth_consumer_key", paramTwitterIdentityProvider.lookupConsumerKey(paramIdentityProviderLoginRequest.applicationId)).withParameter("oauth_token", paramIdentityProviderLoginRequest.data.get("oauth_token")).build()).readTimeout(10000).proxy((ProxyInfo)this.proxyInfoSupplier.get()).bodyHandler((RESTClient.BodyHandler)(new FormDataBodyHandler()).withParameter("oauth_verifier", paramIdentityProviderLoginRequest.data.get("oauth_verifier"))).successResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler()).errorResponseHandler((RESTClient.ResponseHandler)new TextResponseHandler()).post().go();
    paramDebugger.log("Endpoint returned status code [" + clientResponse.status + "]");
    if (clientResponse.status != 200) {
      paramDebugger.handleError(clientResponse, "Request to the [" + str + "] endpoint failed.");
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.TwitterAccessToken);
    } 
    return (ClientResponse)clientResponse;
  }
}
