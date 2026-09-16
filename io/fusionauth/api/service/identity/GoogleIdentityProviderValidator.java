package io.fusionauth.api.service.identity;

import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.api.util.PropertiesTools;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.GoogleApplicationConfiguration;
import io.fusionauth.domain.provider.GoogleIdentityProvider;
import java.util.UUID;

public class GoogleIdentityProviderValidator implements IdentityProviderValidator {
  public Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean) {
    GoogleIdentityProvider googleIdentityProvider = (GoogleIdentityProvider)paramBaseIdentityProvider1;
    return (new Validator())
      .notBlank(googleIdentityProvider.buttonText, "identityProvider.buttonText", new Object[0])
      .notBlank(googleIdentityProvider.client_id, "identityProvider.client_id", new Object[0])
      .notBlank(googleIdentityProvider.client_secret, "identityProvider.client_secret", new Object[0])
      .ifFalse(paramBoolean, paramValidator -> paramValidator.notMissing(paramGoogleIdentityProvider.loginMethod, "identityProvider.loginMethod", new Object[0]))

      
      .ensure(PropertiesTools.validate(googleIdentityProvider.properties.api), "identityProvider.properties.api", "[invalid]", new Object[0])
      .ensure(PropertiesTools.validate(googleIdentityProvider.properties.button), "identityProvider.properties.button", "[invalid]", new Object[0])

      
      .forEach(googleIdentityProvider.applicationConfiguration.keySet(), (paramValidator, paramUUID, paramInteger) -> {
          GoogleApplicationConfiguration googleApplicationConfiguration = paramGoogleIdentityProvider.applicationConfiguration.get(paramUUID);
          paramValidator.ensureWithCode(PropertiesTools.validate(googleApplicationConfiguration.properties.api), "identityProvider.applicationConfiguration[" + String.valueOf(paramUUID) + "].properties.api", "[invalid]identityProvider.applicationConfiguration.properties.api", new Object[0]).ensureWithCode(PropertiesTools.validate(googleApplicationConfiguration.properties.button), "identityProvider.applicationConfiguration[" + String.valueOf(paramUUID) + "].properties.button", "[invalid]identityProvider.applicationConfiguration.properties.button", new Object[0]);
        }).done();
  }
}
