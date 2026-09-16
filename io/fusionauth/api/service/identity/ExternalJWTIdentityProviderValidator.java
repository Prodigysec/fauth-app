package io.fusionauth.api.service.identity;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.domain.IdentityProviderMapper;
import io.fusionauth.api.util.KeyValidator;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.ExternalJWTIdentityProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class ExternalJWTIdentityProviderValidator implements IdentityProviderValidator {
  public static final Set<String> SupportedClaimMappings = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(new String[] { "birthDate", "firstName", "lastName", "fullName", "middleName", "mobilePhone", "imageUrl", "timezone", "UserData", "RegistrationData" })));
  
  private final IdentityProviderMapper identityProviderMapper;
  
  private final KeyValidator keyValidator;
  
  @Inject
  public ExternalJWTIdentityProviderValidator(IdentityProviderMapper paramIdentityProviderMapper, KeyValidator paramKeyValidator) {
    this.identityProviderMapper = paramIdentityProviderMapper;
    this.keyValidator = paramKeyValidator;
  }
  
  public Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean) {
    ExternalJWTIdentityProvider externalJWTIdentityProvider1 = (ExternalJWTIdentityProvider)paramBaseIdentityProvider1;
    ExternalJWTIdentityProvider externalJWTIdentityProvider2 = (paramBaseIdentityProvider2 != null) ? (ExternalJWTIdentityProvider)paramBaseIdentityProvider2 : null;
    Object object = externalJWTIdentityProvider1.domains.isEmpty() ? Collections.emptyList() : this.identityProviderMapper.retrieveExistingDomains(externalJWTIdentityProvider1.domains, externalJWTIdentityProvider1.id, paramBaseIdentityProvider1.tenantId);
    return (new Validator())
      .notBlank(externalJWTIdentityProvider1.headerKeyParameter, "identityProvider.headerKeyParameter", new Object[0])

      
      .forEach(externalJWTIdentityProvider1.claimMap.keySet(), (paramValidator, paramString, paramInteger) -> paramValidator.ensureWithCode(SupportedClaimMappings.contains(paramExternalJWTIdentityProvider.claimMap.get(paramString)), "identityProvider.claimMap[" + paramString + "]", "[invalid]identityProvider.claimMap", new Object[] { paramExternalJWTIdentityProvider.claimMap.get(paramString), paramString, String.join(", ", (Iterable)SupportedClaimMappings) })).ensureWithCode((externalJWTIdentityProvider1.defaultKeyId != null || !externalJWTIdentityProvider1.verificationKeyIds.isEmpty()), "identityProvider.verificationKeyIds", "[empty]identityProvider.verificationKeyIds", new Object[0])




      
      .ifTrue((externalJWTIdentityProvider1.defaultKeyId != null), paramValidator -> {
          boolean bool = (paramExternalJWTIdentityProvider1 == null || !Objects.equals(paramExternalJWTIdentityProvider2.defaultKeyId, paramExternalJWTIdentityProvider1.defaultKeyId)) ? true : false;
          if (bool)
            this.keyValidator.validateExternalJwtVerificationKey(paramValidator, paramExternalJWTIdentityProvider2.defaultKeyId, "identityProvider.defaultKeyId"); 
        }).forEach(externalJWTIdentityProvider1.verificationKeyIds, (paramValidator, paramUUID, paramInteger) -> {
          boolean bool = (paramExternalJWTIdentityProvider == null || !paramExternalJWTIdentityProvider.verificationKeyIds.contains(paramUUID)) ? true : false;
          if (bool)
            this.keyValidator.validateExternalJwtVerificationKey(paramValidator, paramUUID, "identityProvider.verificationKeyIds[" + paramInteger + "]", "[cannotVerify]identityProvider.verificationKeyIds"); 
        }).ifTrue(!externalJWTIdentityProvider1.domains.isEmpty(), paramValidator -> paramValidator.emptyWithCode(paramList, "identityProvider.domains", "[duplicate]identityProvider.domains", new Object[] { String.join(", ", paramList) })).done();
  }
}
