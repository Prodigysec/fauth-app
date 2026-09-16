package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.SonyPSNApplicationConfiguration;
import io.fusionauth.domain.provider.SonyPSNIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _SonyPSNIdentityProvider extends SonyPSNIdentityProvider {
  public void setConfigurations(List<_SonyPSNApplicationConfiguration> paramList) {
    for (_SonyPSNApplicationConfiguration _SonyPSNApplicationConfiguration : paramList) {
      UUID uUID = _SonyPSNApplicationConfiguration.applicationId;
      _SonyPSNApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _SonyPSNApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _SonyPSNApplicationConfiguration extends SonyPSNApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
