package io.fusionauth.api.domain.mybatis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fusionauth.domain.provider.IdentityProviderOauth2Configuration;
import io.fusionauth.domain.provider.OpenIdConnectApplicationConfiguration;
import io.fusionauth.domain.provider.OpenIdConnectIdentityProvider;
import java.util.List;
import java.util.UUID;

public class _OpenIdConnectIdentityProvider extends OpenIdConnectIdentityProvider {
  public void setConfigurations(List<_OpenIDConnectApplicationConfiguration> paramList) {
    for (_OpenIDConnectApplicationConfiguration _OpenIDConnectApplicationConfiguration : paramList) {
      UUID uUID = _OpenIDConnectApplicationConfiguration.applicationId;
      _OpenIDConnectApplicationConfiguration.applicationId = null;
      if (_OpenIDConnectApplicationConfiguration.oauth2 == null)
        _OpenIDConnectApplicationConfiguration.oauth2 = new IdentityProviderOauth2Configuration(); 
      this.applicationConfiguration.put(uUID, _OpenIDConnectApplicationConfiguration);
    } 
  }
  
  public void setTenantConfigurations(List<_IdentityProviderTenantConfiguration> paramList) {
    for (_IdentityProviderTenantConfiguration _IdentityProviderTenantConfiguration : paramList) {
      UUID uUID = _IdentityProviderTenantConfiguration.tenantId;
      _IdentityProviderTenantConfiguration.tenantId = null;
      this.tenantConfiguration.put(uUID, _IdentityProviderTenantConfiguration);
    } 
  }
  
  public static class _OpenIDConnectApplicationConfiguration extends OpenIdConnectApplicationConfiguration {
    @JsonIgnore
    public UUID applicationId;
  }
}
