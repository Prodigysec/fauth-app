package io.fusionauth.api.util;

import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;

public class IdentityProviderTools {
  public static boolean isIdpInitAble(BaseIdentityProvider<?> paramBaseIdentityProvider) {
    if (!(paramBaseIdentityProvider instanceof io.fusionauth.domain.provider.SAMLv2IdPInitiatedIdentityProvider)) {
      if (paramBaseIdentityProvider instanceof SAMLv2IdentityProvider) {
        SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)paramBaseIdentityProvider;
        if (sAMLv2IdentityProvider.idpInitiatedConfiguration.enabled);
      } 
      return false;
    } 
  }
}
