package io.fusionauth.api.service.identity;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.XboxIdentityProvider;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;

public class XboxIdentityProviderValidator implements IdentityProviderValidator {
  private final ReactorStatusService reactorStatusService;
  
  @Inject
  public XboxIdentityProviderValidator(ReactorStatusService paramReactorStatusService) {
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean) {
    XboxIdentityProvider xboxIdentityProvider = (XboxIdentityProvider)paramBaseIdentityProvider1;
    return (new Validator())
      .notBlank(xboxIdentityProvider.buttonText, "identityProvider.buttonText", new Object[0])
      .notBlank(xboxIdentityProvider.client_id, "identityProvider.client_id", new Object[0])
      .notBlank(xboxIdentityProvider.client_secret, "identityProvider.client_secret", new Object[0])
      
      .ifTrue((paramBoolean || !xboxIdentityProvider.enabled), paramValidator -> paramValidator.ensure(ReactorStatusValidator.isLicensedFor(this.reactorStatusService.retrieveStatus(), ()), "identityProvider.type", "[notLicensed]", new Object[] { IdentityProviderType.Xbox })).done();
  }
}
