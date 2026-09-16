package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.EpicGamesApplicationConfiguration;
import io.fusionauth.domain.provider.EpicGamesIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _EpicGamesIdentityProvider extends EpicGamesIdentityProvider {
  public void setConfigurations(List<_EpicGamesApplicationConfiguration> paramList) {
    for (_EpicGamesApplicationConfiguration _EpicGamesApplicationConfiguration : paramList) {
      UUID uUID = _EpicGamesApplicationConfiguration.applicationId;
      _EpicGamesApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _EpicGamesApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _EpicGamesApplicationConfiguration extends EpicGamesApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
