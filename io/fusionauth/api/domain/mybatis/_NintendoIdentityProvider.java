package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.NintendoApplicationConfiguration;
import io.fusionauth.domain.provider.NintendoIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _NintendoIdentityProvider extends NintendoIdentityProvider {
  public void setConfigurations(List<_NintendoApplicationConfiguration> paramList) {
    for (_NintendoApplicationConfiguration _NintendoApplicationConfiguration : paramList) {
      UUID uUID = _NintendoApplicationConfiguration.applicationId;
      _NintendoApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _NintendoApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _NintendoApplicationConfiguration extends NintendoApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
