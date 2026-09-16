package io.fusionauth.api.service.authentication;

import com.google.inject.Inject;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityProviderLinkMapper;
import io.fusionauth.api.domain.api.hypr.HYPRDeviceList;
import io.fusionauth.api.domain.api.hypr.HYPRState;
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
import io.fusionauth.api.service.user.IdentityTypeHelper;
import io.fusionauth.api.service.user.UserMetricsService;
import io.fusionauth.api.service.user.UserReaderService;
import io.fusionauth.api.service.user.UserService;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderStartLoginRequest;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.HYPRIdentityProvider;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.UnauthenticatedException;

public class HYPRIdentityProviderAuthenticationService extends BaseIdentityProviderAuthenticationService {
  @Inject
  public HYPRIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, ExternalIdentifierReaderService paramExternalIdentifierReaderService, ExternalIdentifierService paramExternalIdentifierService, ExpressionEvaluator paramExpressionEvaluator, EventLogService paramEventLogService, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, LambdaInvocationService paramLambdaInvocationService, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramExpressionEvaluator, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramLambdaInvocationService, paramProxyInfoSupplier, paramUserReaderService, paramUserService, paramUserMetricsService, paramTenantCache);
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    HYPRIdentityProvider hYPRIdentityProvider = (HYPRIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(hYPRIdentityProvider.debug, "HYPR IdP Response Debug Log [" + String.valueOf(hYPRIdentityProvider.id) + "]"), hYPRIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    String str1 = paramIdentityProviderLoginRequest.data.get("code");
    ExternalIdentifier externalIdentifier = this.externalIdentifierReader.retrieveById(str1);
    if (externalIdentifier == null) {
      debugger.log("Reconcile request failed, expired or invalid code [" + str1 + "].\nA 401 status code will be returned.")
        .done();
      throw new UnauthenticatedException();
    } 
    HYPRClient hYPRClient = new HYPRClient(this.proxyInfoSupplier, hYPRIdentityProvider, paramIdentityProviderLoginRequest.applicationId);
    String str2 = externalIdentifier.getAttribute("loginId");
    String str3 = externalIdentifier.getAttribute("requestId");
    HYPRState hYPRState = hYPRClient.retrieveAuthenticationRequestStatus(debugger, str3, str2);
    if (hYPRState == null) {
      debugger.log("No status was returned for the requestId [" + str3 + "], see previous messages.\n A 401 status code will be returned.")
        .done();
      throw new UnauthenticatedException();
    } 
    if (hYPRState == HYPRState.COMPLETED || hYPRState == HYPRState.CANCELED || hYPRState == HYPRState.FAILED)
      this.externalIdentifierService.deleteById(str1); 
    if (hYPRState == HYPRState.COMPLETED) {
      debugger.log("A request status of [" + String.valueOf(hYPRState) + "] was returned, login is complete.");
      loginContext.userId = UUID.fromString(externalIdentifier.getAttribute("userId"));
      loginContext.identityProviderDisplayName = str2;
      return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()) }, ), null);
    } 
    if (hYPRState == HYPRState.CANCELED || hYPRState == HYPRState.FAILED) {
      String str = "A request status of [" + String.valueOf(hYPRState) + "] was returned";
      if (hYPRState == HYPRState.CANCELED) {
        debugger.log(str + ", the login request has been cancelled by the user.")
          .done();
        throw new UserAuthenticationCanceledException();
      } 
      debugger.log(str + ", the login request has failed.")
        .done();
      throw new UserAuthenticationFailedException();
    } 
    debugger.log("A request status of [" + String.valueOf(hYPRState) + "] was returned, login is still pending waiting a user response.")
      .done();
    throw new UserAuthenticationPendingException();
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.HYPR;
  }
  
  public boolean canHandle(HYPRIdentityProvider paramHYPRIdentityProvider, UUID paramUUID, String paramString) {
    Debugger debugger = new Debugger(paramHYPRIdentityProvider.debug, "HYPR IdP request device listing Debug Log");
    HYPRClient hYPRClient = new HYPRClient(this.proxyInfoSupplier, paramHYPRIdentityProvider, paramUUID);
    HYPRDeviceList hYPRDeviceList = hYPRClient.retrieveUserDevices(debugger, paramString);
    debugger.log("Device count [" + hYPRDeviceList.size() + "], the HYPR identity provider " + ((hYPRDeviceList.size() > 0) ? "can" : "cannot") + " handle this request.")
      .done();
    return (hYPRDeviceList.size() > 0);
  }
  
  public StartResult start(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, User paramUser, IdentityProviderStartLoginRequest paramIdentityProviderStartLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) throws UnauthenticatedException {
    HYPRIdentityProvider hYPRIdentityProvider = (HYPRIdentityProvider)paramBaseIdentityProvider;
    Debugger debugger = new Debugger(hYPRIdentityProvider.debug, "HYPR IdP Start Request Debug Log");
    HYPRClient hYPRClient = new HYPRClient(this.proxyInfoSupplier, hYPRIdentityProvider, paramIdentityProviderStartLoginRequest.applicationId);
    String str1 = hYPRClient.startAuthenticationRequest(debugger, paramIdentityProviderStartLoginRequest.loginId);
    if (str1 == null) {
      debugger.done();
      throw new UnauthenticatedException();
    } 
    String str2 = this.externalIdentifierService.createExternalAuthentication(paramTenant, paramUser.id, paramApplication.id, (new ExternalIdentifier.ExternalIdData(paramIdentityProviderStartLoginRequest.state)).with(paramExternalIdData -> paramExternalIdData.setAttribute("loginId", paramIdentityProviderStartLoginRequest.loginId).setAttribute("requestId", paramString).setAttribute("userId", paramUser.id)));
    debugger.done();
    return new StartResult(str2);
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderAuthenticationService.ValidationResult validationResult = new IdentityProviderAuthenticationService.ValidationResult();
    validationResult.identityProvider = paramBaseIdentityProvider;
    String str = paramIdentityProviderLoginRequest.data.get("code");
    Validator validator = (new Validator()).notMissing(str, "data.code", new Object[0]);
    if (!validator.hasErrors()) {
      ExternalIdentifierReaderService.ValidationResult validationResult1 = this.externalIdentifierReader.validate(paramTenant, str, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.ExternalAuthentication });
      validationResult.externalIdentifier = validationResult1.id;
      validationResult.tenant = validationResult1.tenant;
      if (validationResult.externalIdentifier != null) {
        paramIdentityProviderLoginRequest.applicationId = validationResult1.id.applicationId;
        validationResult.application = this.applicationCache.get((validationResult.tenant == null) ? null : validationResult.tenant.id, paramIdentityProviderLoginRequest.applicationId);
        if (validationResult.identityProvider.tenantId != null && validationResult.tenant != null && 

          
          !validationResult.identityProvider.tenantId.equals(validationResult.tenant.id))
          validator.getErrors().addGeneralError("[InvalidIdentityProviderId]", null, new Object[0]); 
      } 
      validator.validObject(validationResult.externalIdentifier, "data.code", new Object[0]);
    } 
    validationResult


      
      .errors = validator.ifTrue((paramExternalIdentifier != null && validationResult.tenant != null), paramValidator -> paramValidator.ensure(paramValidationResult.tenant.id.equals(((ExternalIdentifier)Objects.requireNonNull((T)paramExternalIdentifier)).tenantId), "connectionTestId", "[invalid]", new Object[0])).done();
    return validationResult;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validateStart(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderStartLoginRequest paramIdentityProviderStartLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderAuthenticationService.ValidationResult validationResult = commonValidate(paramTenant, paramIdentityProviderStartLoginRequest.applicationId, paramBaseIdentityProvider, paramExternalIdentifier);
    List<IdentityType> list = IdentityTypeHelper.convert(paramIdentityProviderStartLoginRequest.loginIdTypes);
    validationResult.user = this.userReader.retrieveByLoginId((validationResult.tenant == null) ? null : validationResult.tenant.id, paramIdentityProviderStartLoginRequest.loginId, list);
    validationResult.errors.add((new Validator())
        .notBlank(paramIdentityProviderStartLoginRequest.loginId, "loginId", new Object[0])
        .ifLastCheckHadNoError(paramValidator -> paramValidator.validObject(paramValidationResult.user, "loginId", new Object[] { paramIdentityProviderStartLoginRequest.loginId })).done());
    return validationResult;
  }
}
