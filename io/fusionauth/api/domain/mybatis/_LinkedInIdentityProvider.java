package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.LinkedInApplicationConfiguration;
import io.fusionauth.domain.provider.LinkedInIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _LinkedInIdentityProvider extends LinkedInIdentityProvider {
  public void setConfigurations(List<_LinkedInApplicationConfiguration> paramList) {
    for (_LinkedInApplicationConfiguration _LinkedInApplicationConfiguration : paramList) {
      UUID uUID = _LinkedInApplicationConfiguration.applicationId;
      _LinkedInApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _LinkedInApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _LinkedInApplicationConfiguration extends LinkedInApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
