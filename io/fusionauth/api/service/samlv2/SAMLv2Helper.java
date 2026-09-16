package io.fusionauth.api.service.samlv2;

import io.fusionauth.domain.provider.BaseSAMLv2IdentityProvider;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import java.util.UUID;

public class SAMLv2Helper {
  public static String getACS(String paramString) {
    return paramString + "/samlv2/acs";
  }
  
  public static String getIdentityProviderEntityId(String paramString, UUID paramUUID) {
    return paramString + "/samlv2/" + paramString;
  }
  
  public static String getServiceProviderEntityId(String paramString, BaseSAMLv2IdentityProvider<?> paramBaseSAMLv2IdentityProvider) {
    if (paramBaseSAMLv2IdentityProvider instanceof SAMLv2IdentityProvider) {
      SAMLv2IdentityProvider sAMLv2IdentityProvider = (SAMLv2IdentityProvider)paramBaseSAMLv2IdentityProvider;
      if (sAMLv2IdentityProvider.issuer != null)
        return sAMLv2IdentityProvider.issuer; 
    } 
    return paramString + "/samlv2/sp/" + paramString;
  }
}
