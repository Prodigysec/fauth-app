package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.HYPRApplicationConfiguration;
import io.fusionauth.domain.provider.HYPRIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _HYPRIdentityProvider extends HYPRIdentityProvider {
  public void setConfigurations(List<_HYPRApplicationConfiguration> paramList) {
    for (_HYPRApplicationConfiguration _HYPRApplicationConfiguration : paramList) {
      UUID uUID = _HYPRApplicationConfiguration.applicationId;
      _HYPRApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _HYPRApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _HYPRApplicationConfiguration extends HYPRApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
