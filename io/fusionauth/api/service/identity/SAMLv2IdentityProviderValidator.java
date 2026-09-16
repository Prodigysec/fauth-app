package io.fusionauth.api.service.identity;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.IdentityProviderMapper;
import io.fusionauth.api.service.reactor.ReactorStatusService;
import io.fusionauth.api.service.reactor.ReactorStatusValidator;
import io.fusionauth.api.util.KeyValidator;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SAMLv2IdentityProviderValidator implements IdentityProviderValidator {
  private final IdentityProviderMapper identityProviderMapper;
  
  private final KeyValidator keyValidator;
  
  private final ReactorStatusService reactorStatusService;
  
  @Inject
  public SAMLv2IdentityProviderValidator(IdentityProviderMapper paramIdentityProviderMapper, KeyValidator paramKeyValidator, ReactorStatusService paramReactorStatusService) {
    this.identityProviderMapper = paramIdentityProviderMapper;
    this.keyValidator = paramKeyValidator;
    this.reactorStatusService = paramReactorStatusService;
  }
  
  public Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean) {
    SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)paramBaseIdentityProvider1;
    Object object = sAMLv2IdentityProvider.domains.isEmpty() ? Collections.emptyList() : this.identityProviderMapper.retrieveExistingDomains(sAMLv2IdentityProvider.domains, sAMLv2IdentityProvider.id, paramBaseIdentityProvider1.tenantId);
    return (new Validator())
      .notBlank(sAMLv2IdentityProvider.idpEndpoint, "identityProvider.idpEndpoint", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.validAbsoluteHttpURL(paramSAMLv2IdentityProvider.idpEndpoint, "identityProvider.idpEndpoint", new Object[0]))

      
      .ifTrue((sAMLv2IdentityProvider.issuer != null), paramValidator -> paramValidator.notBlankWithCode(paramSAMLv2IdentityProvider.issuer, "identityProvider.issuer", "[invalid]identityProvider.issuer", new Object[0]))



      
      .ensureWithCode((sAMLv2IdentityProvider.keyId != null || !sAMLv2IdentityProvider.verificationKeyIds.isEmpty()), "identityProvider.verificationKeyIds", "[empty]identityProvider.verificationKeyIds", new Object[0])



      
      .ifTrue((sAMLv2IdentityProvider.keyId != null), paramValidator -> this.keyValidator.validateVerifyKeyForSAML(paramValidator, paramSAMLv2IdentityProvider.keyId, "identityProvider.keyId"))


      
      .forEach(sAMLv2IdentityProvider.verificationKeyIds, (paramValidator, paramUUID, paramInteger) -> this.keyValidator.validateVerifyKeyForSAML(paramValidator, paramUUID, "identityProvider.verificationKeyIds[" + paramInteger + "]", "[cannotVerify]identityProvider.verificationKeyIds"))


      
      .ifTrue(sAMLv2IdentityProvider.signRequest, paramValidator -> paramValidator.notMissing(paramSAMLv2IdentityProvider.requestSigningKeyId, "identityProvider.requestSigningKeyId", new Object[0]).ifLastCheckHadNoError(()).ifTrue(paramSAMLv2IdentityProvider.postRequest, ()))






      
      .notBlank(sAMLv2IdentityProvider.buttonText, "identityProvider.buttonText", new Object[] { IdentityProviderType.SAMLv2.name() }).ifTrue(sAMLv2IdentityProvider.loginHintConfiguration.enabled, paramValidator -> paramValidator.notBlank(paramSAMLv2IdentityProvider.loginHintConfiguration.parameterName, "identityProvider.loginHintConfiguration.parameterName", new Object[0]))


      
      .ifTrue((sAMLv2IdentityProvider.domains.size() > 0), paramValidator -> paramValidator.emptyWithCode(paramList, "identityProvider.domains", "[duplicate]identityProvider.domains", new Object[] { String.join(", ", paramList) })).ifTrue(sAMLv2IdentityProvider.idpInitiatedConfiguration.enabled, paramValidator -> paramValidator.validate(()).notBlank(paramSAMLv2IdentityProvider.idpInitiatedConfiguration.issuer, "identityProvider.idpInitiatedConfiguration.issuer", new Object[0]))



      
      .ifTrue(sAMLv2IdentityProvider.assertionDecryptionConfiguration.enabled, paramValidator -> paramValidator.notMissing(paramSAMLv2IdentityProvider.assertionDecryptionConfiguration.keyTransportDecryptionKeyId, "identityProvider.assertionDecryptionConfiguration.keyTransportDecryptionKeyId", new Object[0]).ifLastCheckHadNoError(()))




      
      .done();
  }
}
