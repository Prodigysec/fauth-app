package io.fusionauth.api.service.identity;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.SonyPSNIdentityProvider;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;

public class SonyPSNIdentityProviderValidator implements IdentityProviderValidator {
  private final ReactorStatusService reactorStatusService;
  
  @Inject
  public SonyPSNIdentityProviderValidator(ReactorStatusService paramReactorStatusService) {
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean) {
    SonyPSNIdentityProvider sonyPSNIdentityProvider = (SonyPSNIdentityProvider)paramBaseIdentityProvider1;
    return (new Validator())
      .notBlank(sonyPSNIdentityProvider.buttonText, "identityProvider.buttonText", new Object[0])
      .notBlank(sonyPSNIdentityProvider.client_id, "identityProvider.client_id", new Object[0])
      .notBlank(sonyPSNIdentityProvider.client_secret, "identityProvider.client_secret", new Object[0])
      
      .ifTrue((paramBoolean || !sonyPSNIdentityProvider.enabled), paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(this.reactorStatusService.retrieveStatus(), ()), "identityProvider.type", "[notLicensed]", new Object[] { IdentityProviderType.SonyPSN })).done();
  }
}
