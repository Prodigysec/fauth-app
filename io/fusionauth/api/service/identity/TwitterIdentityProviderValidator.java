package io.fusionauth.api.service.identity;

import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.TwitterIdentityProvider;

public class TwitterIdentityProviderValidator implements IdentityProviderValidator {
  public Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean) {
    TwitterIdentityProvider twitterIdentityProvider = (TwitterIdentityProvider)paramBaseIdentityProvider1;
    return (new Validator())
      .notBlank(twitterIdentityProvider.buttonText, "identityProvider.buttonText", new Object[0])
      .notBlank(twitterIdentityProvider.consumerKey, "identityProvider.consumerKey", new Object[0])
      .notBlank(twitterIdentityProvider.consumerSecret, "identityProvider.consumerSecret", new Object[0])
      .done();
  }
}
