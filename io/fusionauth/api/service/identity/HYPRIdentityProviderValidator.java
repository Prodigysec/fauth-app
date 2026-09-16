package io.fusionauth.api.service.identity;

import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.HYPRApplicationConfiguration;
import io.fusionauth.domain.provider.HYPRIdentityProvider;
import java.util.Map;

public class HYPRIdentityProviderValidator implements IdentityProviderValidator {
  public Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean) {
    HYPRIdentityProvider hYPRIdentityProvider = (HYPRIdentityProvider)paramBaseIdentityProvider1;
    return (new Validator())
      .notBlank(hYPRIdentityProvider.relyingPartyApplicationId, "identityProvider.relyingPartyApplicationId", new Object[0])
      .notMissing(hYPRIdentityProvider.relyingPartyURL, "identityProvider.relyingPartyURL", new Object[0])
      .ifLastCheckHadNoError(paramValidator -> paramValidator.validAbsoluteHttpURL(paramHYPRIdentityProvider.relyingPartyURL, "identityProvider.relyingPartyURL", new Object[0]))
      
      .forEach(hYPRIdentityProvider.applicationConfiguration.entrySet(), (paramValidator, paramEntry, paramInteger) -> paramValidator.ifTrue((((HYPRApplicationConfiguration)paramEntry.getValue()).relyingPartyURL != null), ()))



      
      .done();
  }
}
