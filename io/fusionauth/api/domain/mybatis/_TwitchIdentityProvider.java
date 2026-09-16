package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.TwitchApplicationConfiguration;
import io.fusionauth.domain.provider.TwitchIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _TwitchIdentityProvider extends TwitchIdentityProvider {
  public void setConfigurations(List<_TwitchApplicationConfiguration> paramList) {
    for (_TwitchApplicationConfiguration _TwitchApplicationConfiguration : paramList) {
      UUID uUID = _TwitchApplicationConfiguration.applicationId;
      _TwitchApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _TwitchApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _TwitchApplicationConfiguration extends TwitchApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
