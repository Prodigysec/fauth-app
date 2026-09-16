package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.AppleApplicationConfiguration;
import io.fusionauth.domain.provider.AppleIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _AppleIdentityProvider extends AppleIdentityProvider {
  public void setConfigurations(List<_AppleApplicationConfiguration> paramList) {
    for (_AppleApplicationConfiguration _AppleApplicationConfiguration : paramList) {
      UUID uUID = _AppleApplicationConfiguration.applicationId;
      _AppleApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _AppleApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _AppleApplicationConfiguration extends AppleApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
