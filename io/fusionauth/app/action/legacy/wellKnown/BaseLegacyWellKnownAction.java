package io.fusionauth.app.action.legacy.wellKnown;

import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.app.action.BaseFasterAction;
import io.fusionauth.domain.oauth2.OAuthError;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;

public abstract class BaseLegacyWellKnownAction extends BaseFasterAction {
  protected final FusionAuthConfiguration configuration;
  
  protected final ReactorStatusService reactorStatusService;
  
  protected BaseLegacyWellKnownAction(FusionAuthConfiguration paramFusionAuthConfiguration, ReactorStatusService paramReactorStatusService) {
    this.configuration = paramFusionAuthConfiguration;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  protected boolean isDisabled() {
    return !this.configuration.allowSubClaimOverride();
  }
  
  protected boolean isNotLicensed() {
    return ReactorStatusValidator.isNotLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.legacyAdapter);
  }
  
  protected OAuthError buildNotLicensedError() {
    return new OAuthError(OAuthError.OAuthErrorType.not_licensed, OAuthError.OAuthErrorReason.not_licensed, "The Legacy Adapter feature is only supported with a valid license");
  }
}
