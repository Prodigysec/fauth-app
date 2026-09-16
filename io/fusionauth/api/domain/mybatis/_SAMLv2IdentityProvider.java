package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.SAMLv2ApplicationConfiguration;
import io.fusionauth.domain.provider.SAMLv2IdentityProvider;
import java.util.List;
import java.util.UUID;

public class _SAMLv2IdentityProvider extends SAMLv2IdentityProvider {
  public void setConfigurations(List<_SAMLv2ApplicationConfiguration> paramList) {
    for (_SAMLv2ApplicationConfiguration _SAMLv2ApplicationConfiguration : paramList) {
      UUID uUID = _SAMLv2ApplicationConfiguration.applicationId;
      _SAMLv2ApplicationConfiguration.applicationId = null;
      this.applicationConfiguration.put(uUID, _SAMLv2ApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _SAMLv2ApplicationConfiguration extends SAMLv2ApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
