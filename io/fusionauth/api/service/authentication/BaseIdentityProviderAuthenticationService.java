package io.fusionauth.api.service.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.inversoft.json.ToString;
import com.inversoft.rest.ClientResponse;
import com.inversoft.rest.JSONResponseHandler;
import com.inversoft.rest.ProxyInfo;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.rest.RESTClient;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityProviderLinkMapper;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.service.NotFoundException;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.identity.IdentityProviderUserService;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.system.EventHelper;
import io.fusionauth.api.service.system.EventLogService;
import io.fusionauth.api.service.system.eventLog.Debugger;
import io.fusionauth.api.service.user.DefaultUserService;
import io.fusionauth.api.service.user.ExternalIdentifierReaderService;
import io.fusionauth.api.service.user.ExternalIdentifierService;
import io.fusionauth.api.service.user.UserMetricsService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.IdentityVerifiedReason;
import io.fusionauth.domain.PasswordType;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.VerificationStrategy;
import io.fusionauth.domain.api.LoginPreventedResponse;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import io.fusionauth.domain.event.UserLoginSuccessEvent;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProviderApplicationConfiguration;
import io.fusionauth.domain.provider.IdentityProviderLinkingStrategy;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.UnauthenticatedException;

public abstract class BaseIdentityProviderAuthenticationService implements IdentityProviderAuthenticationService {
  protected final ApplicationCache applicationCache;
  
  protected final AuthenticationService authenticationService;
  
  protected final EventLogService eventLogService;
  
  protected final ExpressionEvaluator expressionEvaluator;
  
  protected final ExternalIdentifierReaderService externalIdentifierReader;
  
  protected final ExternalIdentifierService externalIdentifierService;
  
  protected final FailedLoginService failedLoginService;
  
  protected final IdentityProviderCache identityProviderCache;
  
  protected final LambdaInvocationService lambdaInvocationService;
  
  protected final ProxyInfoSupplier proxyInfoSupplier;
  
  protected final TenantCache tenantCache;
  
  protected final UserReaderService userReader;
  
  protected final UserService userService;
  
  private final IdentityProviderLinkMapper identityProviderLinkMapper;
  
  private final IdentityProviderUserService identityProviderUserService;
  
  private final UserMetricsService userMetricsService;
  
  protected BaseIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, ExpressionEvaluator paramExpressionEvaluator, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, UserReaderService paramUserReaderService, UserService paramUserService, UserMetricsService paramUserMetricsService, TenantCache paramTenantCache) {
    this.applicationCache = paramApplicationCache;
    this.authenticationService = paramAuthenticationService;
    this.eventLogService = paramEventLogService;
    this.expressionEvaluator = paramExpressionEvaluator;
    this.externalIdentifierReader = paramExternalIdentifierReaderService;
    this.externalIdentifierService = paramExternalIdentifierService;
    this.failedLoginService = paramFailedLoginService;
    this.identityProviderCache = paramIdentityProviderCache;
    this.identityProviderLinkMapper = paramIdentityProviderLinkMapper;
    this.identityProviderUserService = paramIdentityProviderUserService;
    this.lambdaInvocationService = paramLambdaInvocationService;
    this.proxyInfoSupplier = paramProxyInfoSupplier;
    this.tenantCache = paramTenantCache;
    this.userMetricsService = paramUserMetricsService;
    this.userReader = paramUserReaderService;
    this.userService = paramUserService;
  }
  
  public <T> T getEnabledIdp(UUID paramUUID1, UUID paramUUID2) {
    BaseIdentityProvider baseIdentityProvider = (BaseIdentityProvider)this.identityProviderCache.get(paramUUID2);
    if (!baseIdentityProvider.isEnabledForApplicationId(paramUUID1))
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.InvalidApplication); 
    return (T)baseIdentityProvider;
  }
  
  public AuthenticationService.AuthenticationResult login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) throws UnauthenticatedException {
    AuthenticationService.AuthenticationResult authenticationResult = _login(paramTenant, paramApplication, paramBaseIdentityProvider, paramIdentityProviderLoginRequest, paramExternalIdentifier);
    if (authenticationResult.rawLogin != null)
      this.userMetricsService.addToLoginQueue(authenticationResult.rawLogin); 
    return authenticationResult;
  }
  
  protected <T> void applyAttributeMappings(JsonNode paramJsonNode, T paramT, Map<String, String> paramMap, boolean paramBoolean) {
    String str = paramBoolean ? "user." : "registration.";
    int i = str.length();
    for (Map.Entry<String, String> entry : paramMap.entrySet()) {
      Object object;
      String str1 = (String)entry.getKey();
      String str2 = (String)entry.getValue();
      if (!str1.startsWith(str))
        continue; 
      String str3 = str1.substring(i);
      try {
        object = extractIdpValue(paramJsonNode, str2);
      } catch (Exception exception) {
        continue;
      } 
      if (object == null)
        continue; 
      try {
        this.expressionEvaluator.setValue(str3, paramT, object);
      } catch (Exception exception) {}
    } 
  }
  
  protected IdentityProviderAuthenticationService.ValidationResult commonValidate(Tenant paramTenant, UUID paramUUID, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, @Nullable ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderAuthenticationService.ValidationResult validationResult = new IdentityProviderAuthenticationService.ValidationResult();
    validationResult.identityProvider = paramBaseIdentityProvider;
    validationResult.connectionTestId = paramExternalIdentifier;
    loadApplication(validationResult, paramTenant, paramUUID);
    validationResult





      
      .errors = (new Validator()).notMissing(paramUUID, "applicationId", new Object[0]).ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramValidationResult.application, "applicationId", new Object[] { paramUUID })).ifTrue((validationResult.connectionTestId != null && validationResult.tenant != null), paramValidator -> paramValidator.ensure(paramValidationResult.tenant.id.equals(((ExternalIdentifier)Objects.requireNonNull((T)paramValidationResult.connectionTestId)).tenantId), "connectionTestId", "[invalid]", new Object[0])).done();
    if (validationResult.identityProvider.tenantId != null && validationResult.tenant != null && 

      
      !validationResult.identityProvider.tenantId.equals(validationResult.tenant.id))
      validationResult.errors.addGeneralError("[InvalidIdentityProviderId]", null, new Object[0]); 
    return validationResult;
  }
  
  protected IdentityProviderAuthenticationService.ValidationResult commonValidateAuthCodeOrToken(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderAuthenticationService.ValidationResult validationResult = commonValidate(paramTenant, paramIdentityProviderLoginRequest.applicationId, paramBaseIdentityProvider, paramExternalIdentifier);
    boolean bool1 = paramIdentityProviderLoginRequest.data.containsKey("code");
    boolean bool2 = paramIdentityProviderLoginRequest.data.containsKey("token");
    validationResult.errors.add((new Validator())
        .ifTrue(bool1, paramValidator -> paramValidator.notMissing(paramIdentityProviderLoginRequest.data.get("redirect_uri"), "data.redirect_uri", new Object[0]))
        
        .ifTrue((!bool2 && !bool1), paramValidator -> paramValidator.ensureWithCode(false, "data.code", "[missing]data.code_or_token", new Object[0]).ensureWithCode(false, "data.token", "[missing]data.code_or_token", new Object[0]))
        
        .done());
    return validationResult;
  }
  
  protected AuthenticationService.AuthenticationResult completeLogin(LoginContext paramLoginContext, Consumer<UserResult> paramConsumer, Function<UserResult, LambdaArgument[]> paramFunction, Function<UserResult, JsonNode> paramFunction1) {
    try {
      return _completeLogin(paramLoginContext, paramConsumer, paramFunction, paramFunction1);
    } finally {
      if (paramLoginContext.connectionTestId != null)
        this.externalIdentifierService.update(paramLoginContext.connectionTestId); 
    } 
  }
  
  protected LambdaArgument[] lambdaArgs(LambdaArgument... paramVarArgs) {
    return paramVarArgs;
  }
  
  protected void loadApplication(IdentityProviderAuthenticationService.ValidationResult paramValidationResult, Tenant paramTenant, UUID paramUUID) {
    paramValidationResult.application = (paramUUID != null) ? this.applicationCache.get((paramTenant != null) ? paramTenant.id : null, paramUUID) : null;
    paramValidationResult.tenant = this.tenantCache.resolve(paramTenant, new Tenantable[] { paramValidationResult.application });
  }
  
  protected JsonNode makeRequest(Debugger paramDebugger, String paramString, Consumer<RESTClient<JsonNode, JsonNode>> paramConsumer, ExternalAuthenticationException.Reason paramReason, RESTClient.HTTPMethod paramHTTPMethod) {
    paramDebugger.log("Call the [" + paramString + "] endpoint.");
    RESTClient<JsonNode, JsonNode> rESTClient = (new RESTClient(JsonNode.class, JsonNode.class)).url(paramString).proxy((ProxyInfo)this.proxyInfoSupplier.get()).readTimeout(10000);
    if (paramConsumer != null)
      paramConsumer.accept(rESTClient); 
    ClientResponse<?, ?> clientResponse = rESTClient.successResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class)).errorResponseHandler((RESTClient.ResponseHandler)new JSONResponseHandler(JsonNode.class)).method(paramHTTPMethod).go();
    paramDebugger.log("Endpoint returned status code [" + clientResponse.status + "]");
    if (!clientResponse.wasSuccessful()) {
      paramDebugger.handleError(clientResponse, "Request to the [" + paramString + "] endpoint failed.");
      throw new ExternalAuthenticationException(paramReason);
    } 
    paramDebugger.logObjectToJSON("Endpoint response:\n", clientResponse.successResponse);
    return (JsonNode)clientResponse.successResponse;
  }
  
  private AuthenticationService.AuthenticationResult _completeLogin(LoginContext paramLoginContext, Consumer<UserResult> paramConsumer, Function<UserResult, LambdaArgument[]> paramFunction, Function<UserResult, JsonNode> paramFunction1) {
    IdentityProviderLinkingStrategy identityProviderLinkingStrategy = paramLoginContext.identityProvider.linkingStrategy;
    paramLoginContext.debugger.log("Linking strategy [" + String.valueOf(identityProviderLinkingStrategy) + "]")
      .log("Resolved email to [" + paramLoginContext.email + "]")
      .log("Resolved username to [" + paramLoginContext.username + "]")
      .log("Resolved unique Id to [" + paramLoginContext.identityProviderUserId + "]");
    if (identityProviderLinkingStrategy != IdentityProviderLinkingStrategy.Unsupported) {
      if (paramLoginContext.identityProviderUserId == null || paramLoginContext.identityProviderUserId.length() == 0) {
        paramLoginContext.debugger.log("A unique Id was not provided by the Identity Provider. The request cannot be completed without a unique Id.")
          .done();
        if (paramLoginContext.connectionTestId != null)
          paramLoginContext.connectionTestId.data.addTraceStep("Unique ID", false, "The identity provider did not provide a unique identifier for the user."); 
        throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.MissingUniqueId);
      } 
      paramLoginContext.debugger.log("Identity provider returned a unique Id [" + paramLoginContext.identityProviderUserId + "].");
      paramLoginContext.link = this.identityProviderLinkMapper.retrieveIdentityProviderLink(paramLoginContext.tenant.id, paramLoginContext.identityProvider.id, paramLoginContext.identityProviderUserId, null);
      if (paramLoginContext.link == null) {
        paramLoginContext.debugger.log("A link has not yet been established for this external user.");
      } else {
        paramLoginContext.debugger.log("User with Id [" + String.valueOf(paramLoginContext.link.userId) + "] is linked to this external user.");
      } 
      if (paramLoginContext.link == null && (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.Disabled || paramLoginContext.request.noLink)) {
        if (paramLoginContext.request.noLink) {
          paramLoginContext.debugger.log("The request was made with noLink=true, throw a NotFoundException which will result in a 404.")
            .done();
        } else {
          paramLoginContext.debugger.log("The request was made with automatic linking disabled, throw a NotFoundException which will result in a 404.")
            .done();
        } 
        if (paramLoginContext.connectionTestId != null)
          paramLoginContext.connectionTestId.data.addTraceStep("Linking disabled", false, "Linking is not enabled or was not requested and no link exists."); 
        throw new NotFoundException();
      } 
      if (paramLoginContext.link == null && identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.CreatePendingLink) {
        if (paramLoginContext.connectionTestId != null) {
          paramLoginContext.connectionTestId.data.addTraceStep("Create pending link", true, "A pending link would be created for the user.");
          AuthenticationService.AuthenticationResult authenticationResult = new AuthenticationService.AuthenticationResult(authenticationType(), null, null);
          authenticationResult.pendingIdPLinkId = "create-pending-link";
          paramLoginContext.debugger.done();
          return authenticationResult;
        } 
        return createPendingLink(paramLoginContext);
      } 
      paramLoginContext.userId = (paramLoginContext.link != null) ? paramLoginContext.link.userId : paramLoginContext.userId;
    } 
    UserResult userResult = getExistingUser(paramLoginContext, paramLoginContext.email, paramLoginContext.username);
    if (userResult == null)
      userResult = buildNewUser(paramLoginContext); 
    if (reconcileUser(paramLoginContext, userResult, paramConsumer, paramFunction, paramFunction1))
      if (paramLoginContext.link == null) {
        UserResult userResult1 = null;
        if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmail || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmailForExistingUser) {
          String str1 = userResult.user.email;
          if (str1 != null && !str1.equals(paramLoginContext.email)) {
            paramLoginContext.debugger.log("The reconciliation set or modified the initially resolved email. Email is now [" + str1 + "]");
            userResult1 = getExistingUser(paramLoginContext, str1, paramLoginContext.username);
          } 
        } else if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsername || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsernameForExistingUser) {
          String str1 = userResult.user.username;
          if (str1 != null && !str1.equals(paramLoginContext.username)) {
            paramLoginContext.debugger.log("The reconciliation set or modified the initially resolved username. Username is now [" + str1 + "]");
            userResult1 = getExistingUser(paramLoginContext, paramLoginContext.email, str1);
          } 
        } 
        if (userResult1 != null) {
          userResult = userResult1;
          paramLoginContext.debugger.log("Reconcile user again because linking data was modified in the initial reconcile.");
          reconcileUser(paramLoginContext, userResult, paramConsumer, paramFunction, paramFunction1);
        } 
        String str = userResult.user.email;
        if (str != null && !str.equals(paramLoginContext.email))
          if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmail || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmailForExistingUser)
            if (!userResult.user.identities.isEmpty()) {
              UserIdentity userIdentity = resolvePrimaryIdentityForLinkingStrategy(userResult.user, IdentityProviderLinkingStrategy.LinkByEmail);
              if (userIdentity != null) {
                userIdentity.verifiedReason = IdentityVerifiedReason.Trusted;
                userIdentity.value = str;
              } 
            } else {
              UserIdentity userIdentity = (new UserIdentity()).with(paramUserIdentity -> paramUserIdentity.type = IdentityType.email).with(paramUserIdentity -> paramUserIdentity.primary = true).with(paramUserIdentity -> paramUserIdentity.verified = false).with(paramUserIdentity -> paramUserIdentity.verifiedReason = IdentityVerifiedReason.Trusted).with(paramUserIdentity -> paramUserIdentity.value = paramString);
              userResult.user.identities.add(userIdentity);
            }   
      } else {
        UserIdentity userIdentity = userResult.user.resolvePrimaryIdentity(IdentityType.email);
        if (userIdentity != null)
          userResult.user.email = userIdentity.value; 
        userIdentity = userResult.user.resolvePrimaryIdentity(IdentityType.username);
        if (userIdentity != null)
          userResult.user.username = userIdentity.value; 
        userIdentity = userResult.user.resolvePrimaryIdentity(IdentityType.phoneNumber);
        if (userIdentity != null)
          userResult.user.phoneNumber = userIdentity.value; 
      }  
    if (paramLoginContext.link == null)
      if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmail || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmailForExistingUser) {
        String str = userResult.user.email;
        if (str == null || str.length() == 0) {
          paramLoginContext.debugger.log("The identity provider was unable to reconcile the email address. An email address is required to complete this request and link by email.")
            .done();
          if (paramLoginContext.connectionTestId != null)
            paramLoginContext.connectionTestId.data.addTraceStep("Missing email", false, "Unable to reconcile email address to link user by email."); 
          throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.MissingEmail);
        } 
        if (paramLoginContext.email_verified != null && !Boolean.parseBoolean(paramLoginContext.email_verified)) {
          paramLoginContext.debugger.log("A link cannot be established using the provided email address because it has not been verified.")
            .done();
          if (paramLoginContext.connectionTestId != null)
            paramLoginContext.connectionTestId.data.addTraceStep("Unverified email", false, "A link cannot be established using an unverified email address."); 
          throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.UnverifiedEmail);
        } 
      } else if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsername || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsernameForExistingUser) {
        String str = userResult.user.username;
        if (str == null || str.length() == 0) {
          paramLoginContext.debugger.log("The identity provider was unable to reconcile the username. A username is required to complete this request and link by username.")
            .done();
          if (paramLoginContext.connectionTestId != null)
            paramLoginContext.connectionTestId.data.addTraceStep("Missing username", false, "Unable to reconcile username to link user by username."); 
          throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.MissingUsername);
        } 
      }  
    if (paramLoginContext.connectionTestId != null) {
      paramLoginContext.connectionTestId.data
        .addTraceStep("Success", true, "The identity provider login successfully reconciled a user.")
        .setAttribute("email", userResult.user.email)
        .setAttribute("username", userResult.user.username)
        .setAttribute("identityProviderUserId", paramLoginContext.identityProviderUserId);
      AuthenticationService.AuthenticationResult authenticationResult = new AuthenticationService.AuthenticationResult(authenticationType(), null, userResult.user);
      authenticationResult.userIdentity = resolvePrimaryIdentityForLinkingStrategy(userResult.user, identityProviderLinkingStrategy);
      paramLoginContext.debugger.done();
      return authenticationResult;
    } 
    return updateUser(paramLoginContext, userResult, paramLoginContext.request.eventInfo);
  }
  
  private UserResult buildNewUser(LoginContext paramLoginContext) {
    Application application = paramLoginContext.application;
    Debugger debugger = paramLoginContext.debugger;
    UUID uUID = paramLoginContext.userId;
    IdentityProviderLinkingStrategy identityProviderLinkingStrategy = paramLoginContext.identityProvider.linkingStrategy;
    if (uUID != null) {
      debugger.log("The identity provider was unable to reconcile the user with Id [" + String.valueOf(uUID) + "]. The user does not exist.")
        .done();
      if (paramLoginContext.connectionTestId != null)
        paramLoginContext.connectionTestId.data.addTraceStep("Missing user", false, "The user that started the request no longer exists."); 
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.MissingUser);
    } 
    User user = new User();
    if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmail || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmailForExistingUser) {
      debugger.log("The user with the email address [" + paramLoginContext.email + "] does not exist.");
      if (paramLoginContext.email != null) {
        user.email = paramLoginContext.email;
        UserIdentity userIdentity = (new UserIdentity()).with(paramUserIdentity -> paramUserIdentity.type = IdentityType.email).with(paramUserIdentity -> paramUserIdentity.primary = true).with(paramUserIdentity -> paramUserIdentity.verifiedReason = IdentityVerifiedReason.Trusted).with(paramUserIdentity -> paramUserIdentity.value = paramLoginContext.email);
        user.identities.add(userIdentity);
      } 
    } else if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsername || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsernameForExistingUser) {
      debugger.log("The user with the username [" + paramLoginContext.username + "] does not exists.");
      if (paramLoginContext.username != null) {
        user.username = paramLoginContext.username;
        UserIdentity userIdentity = (new UserIdentity()).with(paramUserIdentity -> paramUserIdentity.type = IdentityType.username).with(paramUserIdentity -> paramUserIdentity.primary = true).with(paramUserIdentity -> paramUserIdentity.value = paramLoginContext.username);
        user.identities.add(userIdentity);
      } 
    } 
    return new UserResult(null, user, (new UserRegistration())
        
        .with(paramUserRegistration -> paramUserRegistration.applicationId = paramApplication.id));
  }
  
  private UserService.UserResult createLinkAnonymously(LoginContext paramLoginContext, User paramUser, EventInfo paramEventInfo) {
    paramLoginContext

      
      .link = (new IdentityProviderLink()).with(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderId = paramLoginContext.identityProvider.id).with(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderUserId = paramLoginContext.identityProviderUserId).with(paramIdentityProviderLink -> paramIdentityProviderLink.displayName = paramLoginContext.identityProviderDisplayName).with(paramIdentityProviderLink -> paramIdentityProviderLink.token = paramLoginContext.identityProviderToken);
    return this.userService.createUserWithoutIdentity(paramLoginContext.tenant, paramLoginContext.link, paramUser, paramEventInfo);
  }
  
  private AuthenticationService.AuthenticationResult createPendingLink(LoginContext paramLoginContext) {
    AuthenticationService.AuthenticationResult authenticationResult = new AuthenticationService.AuthenticationResult(authenticationType(), null, null);
    ExternalIdentifier.ExternalIdData externalIdData = (new ExternalIdentifier.ExternalIdData()).setAttribute("identityProviderId", paramLoginContext.identityProvider.id).setAttribute("email", paramLoginContext.email).setAttribute("identityProviderName", paramLoginContext.identityProvider.name).setAttribute("identityProviderType", paramLoginContext.identityProvider.getType()).setAttribute("identityProviderDisplayName", paramLoginContext.identityProviderDisplayName).setAttribute("identityProviderToken", paramLoginContext.identityProviderToken).setAttribute("identityProviderUserId", paramLoginContext.identityProviderUserId).setAttribute("username", paramLoginContext.username);
    authenticationResult.pendingIdPLinkId = this.externalIdentifierService.createPendingIdPLinkId(paramLoginContext.tenant, paramLoginContext.application.id, externalIdData);
    paramLoginContext.debugger.log("A pending link was created to allow this user to be linked with the Link API. Link details:")
      .log("IdentityProviderId: %s", new Object[] { paramLoginContext.identityProvider.id }).log("IdentityProviderId: %s", new Object[] { paramLoginContext.identityProvider.id }).log("IdentityProviderName: %s", new Object[] { paramLoginContext.identityProvider.name }).log("IdentityProviderType: %s", new Object[] { paramLoginContext.identityProvider.getType() }).log("IdentityProviderDisplayName: %s", new Object[] { paramLoginContext.identityProviderDisplayName }).log("IdentityProviderUserId: %s", new Object[] { paramLoginContext.identityProviderUserId }).log("Email: %s", new Object[] { paramLoginContext.email }).log("Username: %s", new Object[] { paramLoginContext.username }).log("The link must now be completed using the Link API.", new Object[0])
      .done();
    return authenticationResult;
  }
  
  private Object extractIdpValue(JsonNode paramJsonNode, String paramString) {
    String str = paramString.startsWith("/") ? paramString : ("/" + paramString.replace(".", "/"));
    JsonNode jsonNode = paramJsonNode.at(str);
    if (jsonNode instanceof com.fasterxml.jackson.databind.node.MissingNode || jsonNode instanceof com.fasterxml.jackson.databind.node.NullNode)
      return null; 
    return jsonNode.isValueNode() ? jsonNode.asText() : jsonNode;
  }
  
  private UserResult getExistingUser(LoginContext paramLoginContext, String paramString1, String paramString2) {
    Application application = paramLoginContext.application;
    Debugger debugger = paramLoginContext.debugger;
    Tenant tenant = paramLoginContext.tenant;
    UUID uUID = paramLoginContext.userId;
    IdentityProviderLinkingStrategy identityProviderLinkingStrategy = paramLoginContext.identityProvider.linkingStrategy;
    IdentityProviderLink identityProviderLink = paramLoginContext.link;
    if (uUID == null)
      if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmail || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmailForExistingUser) {
        if (paramString1 == null || paramString1.length() == 0)
          return null; 
      } else if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsername || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsernameForExistingUser) {
        if (paramString2 == null || paramString2.length() == 0)
          return null; 
      }  
    User user1 = null;
    if (uUID != null) {
      user1 = this.userReader.retrieveById(tenant.id, uUID);
      if (user1 == null)
        user1 = this.userReader.retrieveIdentityLessUserById(tenant.id, identityProviderLink.userId); 
    } else if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmail || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmailForExistingUser) {
      user1 = this.userReader.retrieveByLoginId(tenant.id, paramString1, List.of(IdentityType.email));
    } else if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsername || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsernameForExistingUser) {
      user1 = this.userReader.retrieveByLoginId(tenant.id, paramString2, List.of(IdentityType.username));
    } 
    if (user1 == null)
      return null; 
    if (uUID == null)
      if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmail || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmailForExistingUser) {
        debugger.log("The user with the email address [" + paramString1 + "] already exists.");
      } else if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsername || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsernameForExistingUser) {
        debugger.log("The user with the username [" + paramString2 + "] already exists.");
      }  
    if (!user1.active) {
      debugger.log("User account is locked, login will be prevented.")
        .done();
      if (paramLoginContext.connectionTestId != null)
        paramLoginContext.connectionTestId.data.addTraceStep("User locked", false, "The resolved user is locked."); 
      throw new UserLockedException();
    } 
    List<LoginPreventedResponse> list = this.authenticationService.retrieveLoginPreventedActions(user1, application.id);
    if (!list.isEmpty()) {
      debugger.log("User is prevented from login. Current actions preventing login:\n\n" + ToString.toString(list))
        .done();
      if (paramLoginContext.connectionTestId != null)
        paramLoginContext.connectionTestId.data.addTraceStep("Login prevented", false, "The resolved user is prevented from logging in."); 
      throw new LoginPreventedException(list);
    } 
    if (user1.expiry != null && user1.expiry.isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
      debugger.log("User is expired, login will be prevented.")
        .done();
      if (paramLoginContext.connectionTestId != null)
        paramLoginContext.connectionTestId.data.addTraceStep("User expired", false, "The resolved user is expired."); 
      throw new UserExpiredException();
    } 
    UserRegistration userRegistration = user1.getRegistrationForApplication(application.id);
    if (userRegistration == null)
      userRegistration = (new UserRegistration()).with(paramUserRegistration -> paramUserRegistration.applicationId = paramApplication.id); 
    User user2 = new User(user1);
    User user3 = (new User(user2)).secure();
    user3.getRegistrations().clear();
    return new UserResult(user2, user3, userRegistration);
  }
  
  private boolean reconcileUser(LoginContext paramLoginContext, UserResult paramUserResult, Consumer<UserResult> paramConsumer, Function<UserResult, LambdaArgument[]> paramFunction, Function<UserResult, JsonNode> paramFunction1) {
    if (paramConsumer != null)
      paramConsumer.accept(paramUserResult); 
    List<? extends UserIdentity> list = paramUserResult.user.identities.stream().map(UserIdentity::new).toList();
    IdentityProviderLinkingStrategy identityProviderLinkingStrategy = paramLoginContext.identityProvider.linkingStrategy;
    UUID uUID = paramLoginContext.identityProvider.lambdaConfiguration.reconcileId;
    boolean bool = false;
    if (identityProviderLinkingStrategy != IdentityProviderLinkingStrategy.LinkAnonymously && uUID != null) {
      LambdaArgument[] arrayOfLambdaArgument = paramFunction.apply(paramUserResult);
      paramLoginContext.debugger.log("Invoke configured lambda with Id [" + String.valueOf(uUID) + "]");
      this.lambdaInvocationService.invoke(uUID, arrayOfLambdaArgument);
      bool = true;
    } else if (identityProviderLinkingStrategy != IdentityProviderLinkingStrategy.LinkAnonymously && paramFunction1 != null && 
      
      !paramLoginContext.identityProvider.attributeMappings.isEmpty()) {
      JsonNode jsonNode = paramFunction1.apply(paramUserResult);
      paramLoginContext.debugger.log("Applying attribute mappings from identity provider.");
      applyAttributeMappings(jsonNode, paramUserResult.user, paramLoginContext.identityProvider.attributeMappings, true);
      applyAttributeMappings(jsonNode, paramUserResult.registration, paramLoginContext.identityProvider.attributeMappings, false);
      bool = true;
    } 
    paramUserResult.user.identities.clear();
    paramUserResult.user.identities.addAll(list);
    return bool;
  }
  
  private UserIdentity resolvePrimaryIdentityForLinkingStrategy(User paramUser, IdentityProviderLinkingStrategy paramIdentityProviderLinkingStrategy) {
    if (paramUser == null || paramIdentityProviderLinkingStrategy == null)
      return null; 
    switch (paramIdentityProviderLinkingStrategy) {
      case LinkByEmail:
      case LinkByEmailForExistingUser:
      
      case LinkByUsername:
      case LinkByUsernameForExistingUser:
      
    } 
    return 

      
      null;
  }
  
  private Set<ApplicationRole> sanitizeRoles(Application paramApplication, UserRegistration paramUserRegistration) {
    Map map = (Map)paramApplication.roles.stream().collect(Collectors.toMap(paramApplicationRole -> paramApplicationRole.name, paramApplicationRole -> paramApplicationRole));
    Objects.requireNonNull(map);
    Objects.requireNonNull(map);
    return (Set<ApplicationRole>)paramUserRegistration.roles.stream().filter(map::containsKey).map(map::get).collect(Collectors.toSet());
  }
  
  private AuthenticationService.AuthenticationResult updateUser(LoginContext paramLoginContext, UserResult paramUserResult, EventInfo paramEventInfo) {
    UserService.UserResult userResult;
    Application application = paramLoginContext.application;
    Debugger debugger = paramLoginContext.debugger;
    Tenant tenant = paramLoginContext.tenant;
    IdentityProviderLinkingStrategy identityProviderLinkingStrategy = paramLoginContext.identityProvider.linkingStrategy;
    User user1 = paramUserResult.user;
    DefaultUserService.handleUserPrimaryIdentities(user1);
    if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmail || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmailForExistingUser) {
      user1.removeIdentitiesOfType(IdentityType.phoneNumber);
      user1.removeIdentitiesOfType(IdentityType.username);
    } else if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsername || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsernameForExistingUser) {
      user1.removeIdentitiesOfType(IdentityType.email);
      user1.removeIdentitiesOfType(IdentityType.phoneNumber);
    } 
    user1.normalize();
    User user2 = paramUserResult.existing;
    UserService.RegistrationResult registrationResult = null;
    if (user2 == null) {
      if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmailForExistingUser || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsernameForExistingUser) {
        debugger.log("The Identity Provider is configured to only link to an existing user. The user will not be created and the request cannot be completed.")
          .done();
        throw (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmailForExistingUser) ? 
          new ExternalAuthenticationException(ExternalAuthenticationException.Reason.UserDoesNotExistByEmail) : 
          new ExternalAuthenticationException(ExternalAuthenticationException.Reason.UserDoesNotExistByUsername);
      } 
      user1.password = null;
      if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkAnonymously) {
        user1.removeIdentitiesOfType(IdentityType.email);
        user1.removeIdentitiesOfType(IdentityType.phoneNumber);
        user1.removeIdentitiesOfType(IdentityType.username);
        debugger.log("Creating anonymous user: \n" + ToString.toString((new User(user1)).secure().sort()));
        userResult = createLinkAnonymously(paramLoginContext, user1, paramEventInfo);
      } else {
        debugger.log("Creating user: \n" + ToString.toString((new User(user1)).secure().sort()));
        userResult = this.userService.create(tenant, null, user1, SendSetPasswordIdentityType.doNotSend, false, false, false, false, paramEventInfo, null);
      } 
    } else {
      if (paramLoginContext.link == null) {
        List<IdentityProviderLink> list = this.identityProviderLinkMapper.retrieveIdentityProviderLinksByUserId(paramLoginContext.tenant.id, paramLoginContext.identityProvider.id, paramUserResult.user.id);
        if (!list.isEmpty()) {
          boolean bool3 = (paramLoginContext.identityProvider.linkingStrategy == IdentityProviderLinkingStrategy.LinkByEmail || paramLoginContext.identityProvider.linkingStrategy == IdentityProviderLinkingStrategy.LinkByEmailForExistingUser) ? true : false;
          paramLoginContext.debugger.log("An existing FusionAuth is already linked to this IdP based upon the configured linking strategy.\n\nThis is unexpected and can occur if someone is attempting an account takeover, or the IdP allows for more than one user to have the same email address with a different value for the unique Id claim. While this may or may not be malicious, the login cannot be completed. If you believe the current request is valid, you will need to unlink the referenced user from this IdP.\n\nIdentity Provider Id: %s\nIdentity Provider Name: %s\nIdentity Provider Linking Strategy: %s\n\nIdentity Provider User Id: %s\nIdentity Provider User %s\n\nExisting User Id: %s\nExisting User Identity Provider User Id(s): %s\nExisting User %s\n\n", new Object[] { paramLoginContext.identityProvider.id, paramLoginContext.identityProvider.name, paramLoginContext.identityProvider.linkingStrategy, paramLoginContext.identityProviderUserId, 



















                
                bool3 ? ("Email: " + 
                paramLoginContext.email) : ("Username: " + 
                paramLoginContext.username), paramUserResult.user.id, list
                
                .stream().map(paramIdentityProviderLink -> paramIdentityProviderLink.identityProviderUserId).collect(Collectors.joining(", ")), 
                bool3 ? ("Email: " + 
                paramUserResult.user.email) : ("Username: " + 
                paramUserResult.user.username) }).done();
          throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.ExistingUserAlreadyLinked);
        } 
      } 
      UserIdentity userIdentity1 = resolvePrimaryIdentityForLinkingStrategy(user2, identityProviderLinkingStrategy);
      if (userIdentity1 != null && userIdentity1.verificationRequired()) {
        userIdentity1.verifiedReason = IdentityVerifiedReason.Trusted;
        this.userService.updateIdentityVerifiedReason(user1, userIdentity1);
      } 
      user1.password = null;
      if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmail || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByEmailForExistingUser) {
        user1.replaceIdentities(user2.resolveIdentitiesOfType(IdentityType.phoneNumber));
        user1.replaceIdentities(user2.resolveIdentitiesOfType(IdentityType.username));
      } else if (identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsername || identityProviderLinkingStrategy == IdentityProviderLinkingStrategy.LinkByUsernameForExistingUser) {
        user1.replaceIdentities(user2.resolveIdentitiesOfType(IdentityType.email));
        user1.replaceIdentities(user2.resolveIdentitiesOfType(IdentityType.phoneNumber));
      } 
      debugger.log("Updating user: \n" + ToString.toString((new User(user1)).secure().sort()));
      userResult = this.userService.update(tenant, application, user2, user1, false, PasswordType.PLAINTEXT, paramEventInfo);
    } 
    if (user1.twoFactor.methods.size() > 0);
    if (user1.passwordChangeRequired);
    boolean bool = ((BaseIdentityProviderApplicationConfiguration)paramLoginContext.identityProvider.applicationConfiguration.get(application.id)).createRegistration;
    UserRegistration userRegistration1 = paramUserResult.registration;
    UserRegistration userRegistration2 = (user2 != null) ? this.userReader.retrieveRegistration(tenant.id, user2.id, application.id) : null;
    if (userRegistration2 == null) {
      debugger.log("User is not registered for application with Id [" + String.valueOf(application.id) + "]");
      if (bool) {
        Set<ApplicationRole> set = sanitizeRoles(application, userRegistration1);
        registrationResult = this.userService.createRegistration(tenant, application, user1, userRegistration1, set, false, true, false, paramEventInfo);
      } else {
        debugger.log("The Identity Provider is not configured to register new users. The user will not be registered.");
      } 
    } else {
      debugger.log("User is already registered for application with Id [" + String.valueOf(application.id) + "].");
      Set<ApplicationRole> set = sanitizeRoles(application, userRegistration1);
      registrationResult = this.userService.updateRegistration(tenant, application, user1, userRegistration2, userRegistration1, set, false, paramEventInfo);
    } 
    UUID uUID = user1.id;
    user1 = this.userReader.retrieveById(tenant.id, uUID);
    if (user1 == null)
      user1 = this.userReader.retrieveIdentityLessUserById(tenant.id, uUID); 
    if (paramLoginContext.identityProviderUserId != null)
      this.identityProviderUserService.link(paramEventInfo, tenant, user1, paramLoginContext.identityProvider, paramLoginContext.identityProviderUserId, paramLoginContext.identityProviderDisplayName, paramLoginContext.identityProviderToken, null); 
    UserIdentity userIdentity = resolvePrimaryIdentityForLinkingStrategy(user1, identityProviderLinkingStrategy);
    AuthenticationService.AuthenticationResult authenticationResult = new AuthenticationService.AuthenticationResult(authenticationType(), user1.connectorId, user1);
    authenticationResult.userIdentity = userIdentity;
    if (authenticationResult.userIdentity == null)
      authenticationResult.userIdentity = user1.resolveLegacyIdentity(); 
    authenticationResult.authenticatedNotRegistered = (user1.getRegistrationForApplication(application.id) == null);
    boolean bool1 = (tenant.emailConfiguration.verifyEmail && tenant.emailConfiguration.verificationStrategy == VerificationStrategy.FormField) ? true : false;
    boolean bool2 = (tenant.phoneConfiguration.verifyPhoneNumber && tenant.phoneConfiguration.verificationStrategy == VerificationStrategy.FormField) ? true : false;
    userResult.verificationIds.forEach((paramUserIdentity, paramExternalIdentifier) -> {
          if (paramExternalIdentifier.getAttribute("otp") == null)
            return; 
          if (paramBoolean1 && paramUserIdentity.type.is(IdentityType.email)) {
            if (paramUserIdentity.primary)
              paramAuthenticationResult.emailVerificationId = paramExternalIdentifier.id; 
            if (paramUserIdentity.equals(paramAuthenticationResult.userIdentity))
              paramAuthenticationResult.identityVerificationId = paramExternalIdentifier.id; 
          } 
          if (paramBoolean2 && paramUserIdentity.type.is(IdentityType.phoneNumber))
            if (paramUserIdentity.equals(paramAuthenticationResult.userIdentity))
              paramAuthenticationResult.identityVerificationId = paramExternalIdentifier.id;  
        });
    authenticationResult.registrationVerificationId = (registrationResult != null) ? registrationResult.registrationVerificationId : null;
    authenticationResult.registrationVerificationOneTimeCode = (registrationResult != null) ? registrationResult.registrationVerificationOneTimeCode : null;
    this.userService.reindexUser(user1, application.id);
    AuthenticationService.LoginLambdaValidationContext loginLambdaValidationContext = (new AuthenticationService.LoginLambdaValidationContext(authenticationResult.type)).with(paramLoginLambdaValidationContext -> paramLoginLambdaValidationContext.identityProvider = new AuthenticationService.LoginLambdaValidationContext.IdentityProviderContext(paramLoginContext.identityProvider));
    DefaultAuthenticationService.applyLoginValidationLambda(this.lambdaInvocationService, this.failedLoginService, tenant, application, authenticationResult, loginLambdaValidationContext, paramEventInfo);
    if (!authenticationResult.loginLambdaValidationResult.errors.empty())
      return authenticationResult; 
    authenticationResult
      
      .rawLogin = (userResult.rawLogin != null && !userResult.rawLogin.isIndexOnly()) ? userResult.rawLogin : this.userMetricsService.buildRawLogin(user1, null, user1.lastUpdateInstant, application.id, paramLoginContext.request.eventInfo);
    EventHelper.send(tenant, paramLoginContext.application, new UserLoginSuccessEvent(paramLoginContext.request.eventInfo, application.id, authenticationResult.type
          .name(), paramLoginContext.identityProvider, user1));
    debugger.log("User has successfully been reconciled and logged into FusionAuth.")
      .log("Authentication type: " + String.valueOf(authenticationType()))
      .log("Authentication state: " + (authenticationResult.authenticatedNotRegistered ? "Authenticated, but not registered" : "Authenticated"))
      .done();
    return authenticationResult;
  }
  
  protected abstract AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier);
  
  protected static class LoginContext {
    public final Application application;
    
    public final ExternalIdentifier connectionTestId;
    
    public final Debugger debugger;
    
    public final BaseIdentityProvider<?> identityProvider;
    
    public final IdentityProviderLoginRequest request;
    
    public final Tenant tenant;
    
    public String email;
    
    public String email_verified;
    
    public String identityProviderDisplayName;
    
    public String identityProviderToken;
    
    public String identityProviderUserId;
    
    public IdentityProviderLink link;
    
    public UUID userId;
    
    public String username;
    
    public LoginContext(Tenant param1Tenant, Application param1Application, IdentityProviderLoginRequest param1IdentityProviderLoginRequest, Debugger param1Debugger, BaseIdentityProvider<?> param1BaseIdentityProvider, ExternalIdentifier param1ExternalIdentifier) {
      this.tenant = param1Tenant;
      this.application = param1Application;
      this.debugger = param1Debugger;
      this.request = param1IdentityProviderLoginRequest;
      this.identityProvider = param1BaseIdentityProvider;
      this.connectionTestId = param1ExternalIdentifier;
    }
  }
  
  protected static final class UserResult extends Record {
    private final User existing;
    
    private final User user;
    
    private final UserRegistration registration;
    
    protected UserResult(User param1User1, User param1User2, UserRegistration param1UserRegistration) {
      this.existing = param1User1;
      this.user = param1User2;
      this.registration = param1UserRegistration;
    }
    
    public final String toString() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> toString : (Lio/fusionauth/api/service/authentication/BaseIdentityProviderAuthenticationService$UserResult;)Ljava/lang/String;
      //   6: areturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #1202	-> 0
    }
    
    public final int hashCode() {
      // Byte code:
      //   0: aload_0
      //   1: <illegal opcode> hashCode : (Lio/fusionauth/api/service/authentication/BaseIdentityProviderAuthenticationService$UserResult;)I
      //   6: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #1202	-> 0
    }
    
    public final boolean equals(Object param1Object) {
      // Byte code:
      //   0: aload_0
      //   1: aload_1
      //   2: <illegal opcode> equals : (Lio/fusionauth/api/service/authentication/BaseIdentityProviderAuthenticationService$UserResult;Ljava/lang/Object;)Z
      //   7: ireturn
      // Line number table:
      //   Java source line number -> byte code offset
      //   #1202	-> 0
    }
    
    public User existing() {
      return this.existing;
    }
    
    public User user() {
      return this.user;
    }
    
    public UserRegistration registration() {
      return this.registration;
    }
  }
}
