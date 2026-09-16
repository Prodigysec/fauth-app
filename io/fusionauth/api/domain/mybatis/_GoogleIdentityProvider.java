package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.GoogleApplicationConfiguration;
import io.fusionauth.domain.provider.GoogleIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _GoogleIdentityProvider extends GoogleIdentityProvider {
  public void setConfigurations(List<_GoogleApplicationConfiguration> paramList) {
    for (_GoogleApplicationConfiguration _GoogleApplicationConfiguration : paramList) {
      UUID uUID = _GoogleApplicationConfiguration.applicationId;
      _GoogleApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _GoogleApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _GoogleApplicationConfiguration extends GoogleApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
