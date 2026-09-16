package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.SteamApplicationConfiguration;
import io.fusionauth.domain.provider.SteamIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _SteamIdentityProvider extends SteamIdentityProvider {
  public void setConfigurations(List<_SteamApplicationConfiguration> paramList) {
    for (_SteamApplicationConfiguration _SteamApplicationConfiguration : paramList) {
      UUID uUID = _SteamApplicationConfiguration.applicationId;
      _SteamApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _SteamApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _SteamApplicationConfiguration extends SteamApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
