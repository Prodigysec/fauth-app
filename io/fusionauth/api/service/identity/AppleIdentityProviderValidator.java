package io.fusionauth.api.service.identity;

import com.google.inject.Inject;
import com.inversoft.error.Errors;
import com.inversoft.util.StringTools;
import com.inversoft.validator.Validator;
import io.fusionauth.api.util.KeyValidator;
import io.fusionauth.domain.provider.AppleApplicationConfiguration;
import io.fusionauth.domain.provider.AppleIdentityProvider;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderType;
import java.util.Map;
import java.util.Objects;

public class AppleIdentityProviderValidator implements IdentityProviderValidator {
  private final KeyValidator keyValidator;
  
  @Inject
  public AppleIdentityProviderValidator(KeyValidator paramKeyValidator) {
    this.keyValidator = paramKeyValidator;
  }
  
  public Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean) {
    AppleIdentityProvider appleIdentityProvider1 = (AppleIdentityProvider)paramBaseIdentityProvider1;
    AppleIdentityProvider appleIdentityProvider2 = (paramBaseIdentityProvider2 != null) ? (AppleIdentityProvider)paramBaseIdentityProvider2 : null;
    boolean bool = (!StringTools.isTrimmedEmpty(appleIdentityProvider1.servicesId) || !StringTools.isTrimmedEmpty(appleIdentityProvider1.bundleId)) ? true : false;
    return (new Validator())
      .ensure(bool, "identityProvider.bundleId", "[blank]", new Object[0])
      .notBlank(appleIdentityProvider1.buttonText, "identityProvider.buttonText", new Object[] { IdentityProviderType.Apple.name() }).ensure(bool, "identityProvider.servicesId", "[blank]", new Object[0])
      .notBlank(appleIdentityProvider1.teamId, "identityProvider.teamId", new Object[] { IdentityProviderType.Apple.name() }).notMissing(appleIdentityProvider1.keyId, "identityProvider.keyId", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> {
          boolean bool = (paramAppleIdentityProvider1 == null || !Objects.equals(paramAppleIdentityProvider2.keyId, paramAppleIdentityProvider1.keyId)) ? true : false;
          if (bool)
            this.keyValidator.validateAppleSigningKey(paramValidator, paramAppleIdentityProvider2.keyId, "identityProvider.keyId", "[invalid]identityProvider.keyId", "[cannotSign]identityProvider.keyId"); 
        }).forEach(appleIdentityProvider1.applicationConfiguration.entrySet(), (paramValidator, paramEntry, paramInteger) -> paramValidator.ifTrue((((AppleApplicationConfiguration)paramEntry.getValue()).keyId != null), ()))








      
      .done();
  }
}
