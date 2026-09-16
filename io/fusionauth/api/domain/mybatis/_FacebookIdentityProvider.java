package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.FacebookApplicationConfiguration;
import io.fusionauth.domain.provider.FacebookIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _FacebookIdentityProvider extends FacebookIdentityProvider {
  public void setConfigurations(List<_FacebookApplicationConfiguration> paramList) {
    for (_FacebookApplicationConfiguration _FacebookApplicationConfiguration : paramList) {
      UUID uUID = _FacebookApplicationConfiguration.applicationId;
      _FacebookApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _FacebookApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _FacebookApplicationConfiguration extends FacebookApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
