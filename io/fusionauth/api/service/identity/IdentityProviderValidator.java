package io.fusionauth.api.service.identity;

import com.inversoft.error.Errors;
import io.fusionauth.domain.provider.BaseIdentityProvider;

public interface IdentityProviderValidator {
  Errors validate(BaseIdentityProvider<?> paramBaseIdentityProvider1, BaseIdentityProvider<?> paramBaseIdentityProvider2, boolean paramBoolean);
}
