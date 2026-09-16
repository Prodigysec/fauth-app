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
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.identityProvider.IdentityProviderLoginRequest;
import io.fusionauth.domain.api.identityProvider.IdentityProviderStartLoginRequest;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import io.fusionauth.samlv2.service.SAMLv2Service;
import java.util.Collections;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.primeframework.mvc.parameter.el.ExpressionEvaluator;
import org.primeframework.mvc.security.UnauthenticatedException;

public class SAMLv2IdentityProviderAuthenticationService extends BaseSAMLv2IdentityProviderAuthenticationService {
  @Inject
  public SAMLv2IdentityProviderAuthenticationService(ApplicationCache paramApplicationCache, AuthenticationService paramAuthenticationService, EventLogService paramEventLogService, ExternalIdentifierService paramExternalIdentifierService, ExpressionEvaluator paramExpressionEvaluator, FailedLoginService paramFailedLoginService, IdentityProviderCache paramIdentityProviderCache, IdentityProviderLinkMapper paramIdentityProviderLinkMapper, IdentityProviderUserService paramIdentityProviderUserService, KeyCache paramKeyCache, KeyReaderService paramKeyReaderService, LambdaInvocationService paramLambdaInvocationService, ObjectMapper paramObjectMapper, SAMLv2Service paramSAMLv2Service, ProxyInfoSupplier paramProxyInfoSupplier, TenantCache paramTenantCache, UserMetricsService paramUserMetricsService, UserReaderService paramUserReaderService, UserService paramUserService, ExternalIdentifierReaderService paramExternalIdentifierReaderService) {
    super(paramApplicationCache, paramAuthenticationService, paramEventLogService, paramExpressionEvaluator, paramExternalIdentifierReaderService, paramExternalIdentifierService, paramFailedLoginService, paramIdentityProviderCache, paramIdentityProviderLinkMapper, paramIdentityProviderUserService, paramKeyCache, paramKeyReaderService, paramLambdaInvocationService, paramObjectMapper, paramSAMLv2Service, paramProxyInfoSupplier, paramTenantCache, paramUserMetricsService, paramUserReaderService, paramUserService);
  }
  
  public AuthenticationService.AuthenticationResult _login(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)paramBaseIdentityProvider;
    BaseIdentityProviderAuthenticationService.LoginContext loginContext = new BaseIdentityProviderAuthenticationService.LoginContext(paramTenant, paramApplication, paramIdentityProviderLoginRequest, new Debugger(sAMLv2IdentityProvider.debug, "SAML v2 IdP Response Debug Log for [" + sAMLv2IdentityProvider.name + "] [" + String.valueOf(sAMLv2IdentityProvider.id) + "]"), sAMLv2IdentityProvider, paramExternalIdentifier);
    Debugger debugger = loginContext.debugger;
    BaseSAMLv2IdentityProviderAuthenticationService.InternalSAMLAuthenticationResponse internalSAMLAuthenticationResponse = null;
    try {
      BaseSAMLv2IdentityProviderAuthenticationService.InternalSAMLAuthenticationResponse internalSAMLAuthenticationResponse1;
      internalSAMLAuthenticationResponse = parseSamlResponse(debugger, paramIdentityProviderLoginRequest, sAMLv2IdentityProvider, paramExternalIdentifier);
      if (internalSAMLAuthenticationResponse.inResponseTo == null) {
        if (sAMLv2IdentityProvider.idpInitiatedConfiguration.enabled) {
          internalSAMLAuthenticationResponse1 = handleIdpInitiatedAssertions(paramTenant, paramApplication, internalSAMLAuthenticationResponse, loginContext, sAMLv2IdentityProvider, sAMLv2IdentityProvider.idpInitiatedConfiguration.issuer);
        } else {
          debugger.log("This SAML response was unsolicited, as it did not contain the [InResponseTo] attribute.")
            .done();
          if (loginContext.connectionTestId != null)
            loginContext.connectionTestId.data.addTraceStep("Unsolicited response", false, "The SAML response was unsolicited."); 
          throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLResponseUnsolicited);
        } 
      } else {
        debugger.log("Assert the [InResponseTo] attribute value [" + internalSAMLAuthenticationResponse.inResponseTo + "] is expected.");
        ExternalIdentifier externalIdentifier = (this.externalIdentifierReader.validate(paramTenant, internalSAMLAuthenticationResponse.inResponseTo, new ExternalIdentifier.ExternalIdType[] { ExternalIdentifier.ExternalIdType.SAMLv2AuthNRequest })).id;
        UUID uUID = (externalIdentifier == null) ? null : externalIdentifier.data.getAttributeAsUUID("identityProviderId");
        if (externalIdentifier == null || !sAMLv2IdentityProvider.id.equals(uUID)) {
          debugger.log("This SAML response was not expected or has already been processed.")
            .done();
          if (loginContext.connectionTestId != null)
            loginContext.connectionTestId.data.addTraceStep("Unexpected response", false, "The SAML response does not pair with an existing request."); 
          throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLResponseUnexpectedOrReplayed);
        } 
        SAMLv2IdPHelper.assertDestination(debugger, loginContext.connectionTestId, internalSAMLAuthenticationResponse, sAMLv2IdentityProvider, externalIdentifier.getAttribute("samlDestination"));
        SAMLv2IdPHelper.assertAudience(debugger, loginContext.connectionTestId, internalSAMLAuthenticationResponse.assertion, externalIdentifier.getAttribute("samlAudience"));
        SAMLv2IdPHelper.assertSubjectConditions(debugger, loginContext.connectionTestId, internalSAMLAuthenticationResponse.assertion.subject);
        updateContext(internalSAMLAuthenticationResponse, loginContext, sAMLv2IdentityProvider);
        internalSAMLAuthenticationResponse1 = internalSAMLAuthenticationResponse;
        loginContext.identityProviderDisplayName = (loginContext.email != null) ? loginContext.email : loginContext.username;
        this.externalIdentifierService.deleteById(externalIdentifier.id);
      } 
      return completeLogin(loginContext, null, paramUserResult -> lambdaArgs(new LambdaArgument[] { new MutableLambdaArgument(paramUserResult.user()), new MutableLambdaArgument(paramUserResult.registration()), new ImmutableLambdaArgument(paramInternalSAMLAuthenticationResponse) }, ), paramUserResult -> this.objectMapper.valueToTree(paramInternalSAMLAuthenticationResponse));
    } catch (Exception exception) {
      handleLoginExceptions(debugger, internalSAMLAuthenticationResponse, exception, loginContext.connectionTestId);
      if (loginContext.connectionTestId != null)
        loginContext.connectionTestId.data.addTraceStep("Response parsing", false, "The SAML authentication response could not be parsed or verified."); 
      throw new ExternalAuthenticationException(ExternalAuthenticationException.Reason.SAMLResponse);
    } 
  }
  
  public AuthenticationType authenticationType() {
    return AuthenticationType.SAMLv2;
  }
  
  public StartResult start(Tenant paramTenant, Application paramApplication, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, User paramUser, IdentityProviderStartLoginRequest paramIdentityProviderStartLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) throws UnauthenticatedException {
    String str1;
    SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)paramBaseIdentityProvider;
    Debugger debugger = new Debugger(sAMLv2IdentityProvider.debug, "SAMLv2 IdP Start Request Debug Log");
    if (paramIdentityProviderStartLoginRequest.data == null)
      paramIdentityProviderStartLoginRequest.data = Collections.emptyMap(); 
    if (paramIdentityProviderStartLoginRequest.data.containsKey("requestId")) {
      str1 = paramIdentityProviderStartLoginRequest.data.get("requestId");
      debugger.log("Use the provided one time use SAML Request Id [" + str1 + "]");
    } else {
      str1 = "id" + UUID.randomUUID().toString().replace("-", "");
      debugger.log("Generated a one time use SAML Request Id [" + str1 + "]");
    } 
    ExternalIdentifier.ExternalIdData externalIdData = new ExternalIdentifier.ExternalIdData(paramIdentityProviderStartLoginRequest.state);
    externalIdData.device = null;
    if (paramIdentityProviderStartLoginRequest.data.containsKey("samlDestination")) {
      String str = paramIdentityProviderStartLoginRequest.data.get("samlDestination");
      debugger.log("The optional parameter [samlDestination] was provided on the request, record an expected destination of [" + str + "] for future validation.");
      externalIdData.setAttribute("samlDestination", str);
    } 
    if (paramIdentityProviderStartLoginRequest.data.containsKey("samlAudience")) {
      String str = paramIdentityProviderStartLoginRequest.data.get("samlAudience");
      debugger.log("The optional parameter [samlAudience] was provided on the request, record an expected audience of [" + str + "] for future validation.");
      externalIdData.setAttribute("samlAudience", str);
    } 
    externalIdData.setAttribute("identityProviderId", paramIdentityProviderStartLoginRequest.identityProviderId);
    String str2 = this.externalIdentifierService.createSAMLAuthNRequestId(paramTenant, paramApplication.id, str1, externalIdData);
    debugger.done();
    return new StartResult(str2);
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validate(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderLoginRequest paramIdentityProviderLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    IdentityProviderAuthenticationService.ValidationResult validationResult = commonValidate(paramTenant, paramIdentityProviderLoginRequest.applicationId, paramBaseIdentityProvider, paramExternalIdentifier);
    validationResult.errors.add((new Validator())
        .notMissing(paramIdentityProviderLoginRequest.data.get("samlResponse"), "data.samlResponse", new Object[0])
        .done());
    return validationResult;
  }
  
  public IdentityProviderAuthenticationService.ValidationResult validateStart(Tenant paramTenant, @Nonnull BaseIdentityProvider<?> paramBaseIdentityProvider, IdentityProviderStartLoginRequest paramIdentityProviderStartLoginRequest, @Nullable ExternalIdentifier paramExternalIdentifier) {
    return commonValidate(paramTenant, paramIdentityProviderStartLoginRequest.applicationId, paramBaseIdentityProvider, paramExternalIdentifier);
  }
}
