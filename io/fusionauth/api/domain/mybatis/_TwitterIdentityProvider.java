package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.TwitterApplicationConfiguration;
import io.fusionauth.domain.provider.TwitterIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _TwitterIdentityProvider extends TwitterIdentityProvider {
  public void setConfigurations(List<_TwitterApplicationConfiguration> paramList) {
    for (_TwitterApplicationConfiguration _TwitterApplicationConfiguration : paramList) {
      UUID uUID = _TwitterApplicationConfiguration.applicationId;
      _TwitterApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _TwitterApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _TwitterApplicationConfiguration extends TwitterApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
