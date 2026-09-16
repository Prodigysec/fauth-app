package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.XboxApplicationConfiguration;
import io.fusionauth.domain.provider.XboxIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _XboxIdentityProvider extends XboxIdentityProvider {
  public void setConfigurations(List<_XboxApplicationConfiguration> paramList) {
    for (_XboxApplicationConfiguration _XboxApplicationConfiguration : paramList) {
      UUID uUID = _XboxApplicationConfiguration.applicationId;
      _XboxApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _XboxApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _XboxApplicationConfiguration extends XboxApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
