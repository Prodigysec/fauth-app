package io.fusionauth.api.service.identity;

import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.LinkedInIdentityProvider;

public class LinkedInIdentityProviderValidator implements IdentityProviderValidator {
  public Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean) {
    LinkedInIdentityProvider linkedInIdentityProvider = (LinkedInIdentityProvider)paramBaseIdentityProvider1;
    return (new Validator())
      .notBlank(linkedInIdentityProvider.buttonText, "identityProvider.buttonText", new Object[0])
      .notBlank(linkedInIdentityProvider.client_id, "identityProvider.client_id", new Object[0])
      .notBlank(linkedInIdentityProvider.client_secret, "identityProvider.client_secret", new Object[0])
      .done();
  }
}
