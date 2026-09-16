package io.fusionauth.api.service.identity;

import com.inversoft.error.Errors;
import com.inversoft.validator.Validator;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.FacebookApplicationConfiguration;
import io.fusionauth.domain.provider.FacebookIdentityProvider;
import io.fusionauth.domain.provider.IdentityProviderLoginMethod;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class FacebookIdentityProviderValidator implements IdentityProviderValidator {
  public static final Set<IdentityProviderLoginMethod> SupportedLoginMethods = Set.of(IdentityProviderLoginMethod.UsePopup, IdentityProviderLoginMethod.UseRedirect);
  
  public Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean) {
    FacebookIdentityProvider facebookIdentityProvider = (FacebookIdentityProvider)paramBaseIdentityProvider1;
    return (new Validator())
      .notBlank(facebookIdentityProvider.appId, "identityProvider.appId", new Object[0])
      .notBlank(facebookIdentityProvider.buttonText, "identityProvider.buttonText", new Object[0])
      .notBlank(facebookIdentityProvider.client_secret, "identityProvider.client_secret", new Object[0])
      .ifFalse(paramBoolean, paramValidator -> paramValidator.notMissing(paramFacebookIdentityProvider.loginMethod, "identityProvider.loginMethod", new Object[0]))
      .ifTrue((facebookIdentityProvider.loginMethod != null), paramValidator -> paramValidator.ensure(SupportedLoginMethods.contains(paramFacebookIdentityProvider.loginMethod), "identityProvider.loginMethod", "[invalid]", new Object[] { paramFacebookIdentityProvider.getType(), SupportedLoginMethods.stream().map(Enum::name).collect(Collectors.joining(", ")) })).forEach(facebookIdentityProvider.applicationConfiguration.keySet(), (paramValidator, paramUUID, paramInteger) -> {
          FacebookApplicationConfiguration facebookApplicationConfiguration = paramFacebookIdentityProvider.applicationConfiguration.get(paramUUID);
          paramValidator.ifTrue((facebookApplicationConfiguration.loginMethod != null), ());
        }).done();
  }
}
