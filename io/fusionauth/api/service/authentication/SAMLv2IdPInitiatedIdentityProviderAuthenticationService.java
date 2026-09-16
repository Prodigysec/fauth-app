package io.fusionauth.api.service.authentication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.inversoft.rest.ProxyInfoSupplier;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.ExternalIdentifier;
import io.fusionauth.api.domain.IdentityProviderLinkMapper;
import io.fusionauth.api.domain.api.service.ImmutableLambdaArgument;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.domain.api.service.MutableLambdaArgument;
import io.fusionauth.api.security.KeyCache;
import io.fusionauth.api.service.cache.ApplicationCache;
import io.fusionauth.api.service.cache.IdentityProviderCache;
import io.fusionauth.api.service.cache.TenantCache;
import io.fusionauth.api.service.identity.IdentityProviderUserService;
import io.fusionauth.api.service.lambda.LambdaInvocationService;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.service.system.EventLogService;
import io.fusionauth.api.service.system.KeyReaderService;
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
import io.fusionauth.domain.provider.SAMLv2IdPInitiatedIdentityProvider;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.samlv2.service.SAMLv2Service;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;

public class SAMLv2IdPInitiatedIdentityProviderAuthenticationService extends BaseSAMLv2IdentityProviderAuthenticationService {
  private final ReactorStatusService reactorStatusService;
  
  @Inject
  public SAMLv2IdPInitiatedIdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExternalIdentifierService paramExternalIdentifierService, ExpressionEvaluator paramExpressionEvaluator, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, ExternalIdentifierReaderService paramExternalIdentifierReaderService, KeyCache paramKeyCache, KeyReaderService paramKeyReaderService, LambdaInvocationService paramLambdaInvocationService, ObjectMapper paramObjectMapper, SAMLv2Service paramSAMLv2Service, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService, ReactorStatusService paramReactorStatusService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExpressionEvaluator, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramKeyCache, paramKeyReaderService, paramLambdaInvocationService, paramObjectMapper, paramSAMLv2Service, paramProxyInfoSupplier, paramTenantCache, paramUserMetricsService, paramUserReaderService, paramUserService);
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    SAMLv2IdPInitiatedIdentityProvider sAMLv2IdPInitiatedIdentityProvider = (SAMLv2IdPInitiatedIdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(sAMLv2IdPInitiatedIdentityProvider.debug, "SAML v2 IdP Initiated Login Response Debug Log for [" + sAMLv2IdPInitiatedIdentityProvider.name + "] [" + String.valueOf(sAMLv2IdPInitiatedIdentityProvider.id) + "]"), sAMLv2IdPInitiatedIdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    BaseSAMLv2IdentityProviderAuthenticationService.InternalSAMLAuthenticationResponse internalSAMLAuthenticationResponse = null;
    try {
      internalSAMLAuthenticationResponse = parseSamlResponse(debugger, paramIdentityProviderLoginRequest, sAMLv2IdPInitiatedIdentityProvider, paramExternalIdentifier);
      if (internalSAMLAuthenticationResponse.inResponseTo != null) {
        debugger.log("While this SAML response was not expected to be solicited, this request contains the [InResponseTo] attribute which means it was in response to a request initiated by FusionAuth or another party.")
          .done();
        throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLIdPInitiatedResponseSolicited);
      } 
      BaseSAMLv2IdentityProviderAuthenticationService.InternalSAMLAuthenticationResponse internalSAMLAuthenticationResponse1 = handleIdpInitiatedAssertions(paramTenant, paramApplication, internalSAMLAuthenticationResponse, loginContext, sAMLv2IdPInitiatedIdentityProvider, sAMLv2IdPInitiatedIdentityProvider.issuer);
      return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramInternalSAMLAuthenticationResponse) }, ), paramUserResult -> this.objectMapper.valueToTree(paramInternalSAMLAuthenticationResponse));
    } catch (Exception exception) {
      handleLoginExceptions(debugger, internalSAMLAuthenticationResponse, exception, paramExternalIdentifier);
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLResponse);
    } 
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.SAMLv2IdpInitiated;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderAuthenticationService.ValidationResult validationResult = commonValidate(paramTenant, paramIdentityProviderLoginRequest.applicationId, paramBaseIdentityProvider, paramExternalIdentifier);
    validationResult.errors.add((new Validator())
        
        .ensure(ReactorStatusValidator.isLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.advancedIdentityProviders), "identityProviderId", "[notLicensed]", new Object[0])
        .notMissing(paramIdentityProviderLoginRequest.data.get("samlResponse"), "data.samlResponse", new Object[0])
        .done());
    return validationResult;
  }
}
