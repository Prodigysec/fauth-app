package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.ExternalJWTApplicationConfiguration;
import io.fusionauth.domain.provider.ExternalJWTIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _ExternalJWTIdentityProvider extends ExternalJWTIdentityProvider {
  public void setConfigurations(List<_ExternalJWTApplicationConfiguration> paramList) {
    for (_ExternalJWTApplicationConfiguration _ExternalJWTApplicationConfiguration : paramList) {
      UUID uUID = _ExternalJWTApplicationConfiguration.applicationId;
      _ExternalJWTApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _ExternalJWTApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _ExternalJWTApplicationConfiguration extends ExternalJWTApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
