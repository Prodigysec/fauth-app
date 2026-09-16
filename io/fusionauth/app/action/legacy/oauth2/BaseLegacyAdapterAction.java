package io.fusionauth.app.action.legacy.oauth2;

import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.guice.FusionAuthClientProvider;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.app.action.NoStoreJSONBaseAction;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import io.fusionauth.http.server.HTTPRequest;
import java.util.UUID;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

public abstract class BaseLegacyAdapterAction extends NoStoreJSONBaseAction {
  protected final FusionAuthConfiguration configuration;
  
  protected final FusionAuthClientProvider fusionAuthClientProvider;
  
  protected final HTTPRequest httpRequest;
  
  protected final ReactorStatusService reactorStatusService;
  
  public UUID tenantId;
  
  protected BaseLegacyAdapterAction(FusionAuthConfiguration paramFusionAuthConfiguration, FusionAuthClientProvider paramFusionAuthClientProvider, HTTPRequest paramHTTPRequest, ReactorStatusService paramReactorStatusService) {
    this.configuration = paramFusionAuthConfiguration;
    this.fusionAuthClientProvider = paramFusionAuthClientProvider;
    this.httpRequest = paramHTTPRequest;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  @PostParameterMethod
  public void setupRequestContext() {
    ActionTools.resolveTenantIdFromHeader(this.httpRequest).ifPresent(paramUUID -> this.tenantId = paramUUID);
  }
  
  protected OAuthError buildNotLicensedError() {
    return new OAuthError(OAuthError.OAuthErrorType.not_licensed, OAuthError.OAuthErrorReason.not_licensed, "The Legacy Adapter feature is only supported with a valid license");
  }
  
  protected String extractBearerToken() {
    return ActionTools.extractBearerTokenFromAuthorizationHeader(this.httpRequest.getHeader("Authorization")).orElse(null);
  }
  
  protected boolean isDisabled() {
    return !this.configuration.allowSubClaimOverride();
  }
  
  protected boolean isNotLicensed() {
    return ReactorStatusValidator.isNotLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.legacyAdapter);
  }
}
