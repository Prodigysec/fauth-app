package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.SAMLv2IdPInitiatedApplicationConfiguration;
import io.fusionauth.domain.provider.SAMLv2IdPInitiatedIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _SAMLv2IdPInitiatedIdentityProvider extends SAMLv2IdPInitiatedIdentityProvider {
  public void setConfigurations(List<_SAMLv2IdPInitiatedApplicationConfiguration> paramList) {
    for (_SAMLv2IdPInitiatedApplicationConfiguration _SAMLv2IdPInitiatedApplicationConfiguration : paramList) {
      UUID uUID = _SAMLv2IdPInitiatedApplicationConfiguration.applicationId;
      _SAMLv2IdPInitiatedApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _SAMLv2IdPInitiatedApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _SAMLv2IdPInitiatedApplicationConfiguration extends SAMLv2IdPInitiatedApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
