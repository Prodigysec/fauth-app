package io.fusionauth.api.service.identity;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.util.KeyValidator;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.SAMLv2IdPInitiatedIdentityProvider;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;

public class SAMLv2IdPInitiatedIdentityProviderValidator implements IdentityProviderValidator {
  private final KeyValidator keyValidator;
  
  private final ReactorStatusService reactorStatusService;
  
  @Inject
  public SAMLv2IdPInitiatedIdentityProviderValidator(KeyValidator paramKeyValidator, ReactorStatusService paramReactorStatusService) {
    this.keyValidator = paramKeyValidator;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean) {
    SAMLv2IdPInitiatedIdentityProvider sAMLv2IdPInitiatedIdentityProvider = (SAMLv2IdPInitiatedIdentityProvider)paramBaseIdentityProvider1;
    return (new Validator())
      
      .ensure(ReactorStatusValidator.isLicensedFor(this.reactorStatusService.retrieveStatus(), paramReactorStatus -> paramReactorStatus.advancedIdentityProviders), "identityProvider.type", "[notLicensed]", new Object[] { IdentityProviderType.SAMLv2IdPInitiated }).notBlank(sAMLv2IdPInitiatedIdentityProvider.issuer, "identityProvider.issuer", new Object[0])

      
      .notMissing(sAMLv2IdPInitiatedIdentityProvider.keyId, "identityProvider.keyId", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> this.keyValidator.validateVerifyKeyForSAML(paramValidator, paramSAMLv2IdPInitiatedIdentityProvider.keyId, "identityProvider.keyId"))

      
      .ifTrue(sAMLv2IdPInitiatedIdentityProvider.assertionDecryptionConfiguration.enabled, paramValidator -> paramValidator.notMissing(paramSAMLv2IdPInitiatedIdentityProvider.assertionDecryptionConfiguration.keyTransportDecryptionKeyId, "identityProvider.assertionDecryptionConfiguration.keyTransportDecryptionKeyId", new Object[0]).ifLastCheckHadNoError(()))




      
      .done();
  }
}
