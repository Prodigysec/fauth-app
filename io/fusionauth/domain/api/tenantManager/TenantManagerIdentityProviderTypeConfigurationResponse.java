package io.fusionauth.domain.api.tenantManager;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.tenantManager.TenantManagerIdentityProviderTypeConfiguration;

public class TenantManagerIdentityProviderTypeConfigurationResponse {
  public TenantManagerIdentityProviderTypeConfiguration typeConfiguration;
  
  @JacksonConstructor
  public TenantManagerIdentityProviderTypeConfigurationResponse() {}
  
  public TenantManagerIdentityProviderTypeConfigurationResponse(TenantManagerIdentityProviderTypeConfiguration paramTenantManagerIdentityProviderTypeConfiguration) {
    this.typeConfiguration = paramTenantManagerIdentityProviderTypeConfiguration;
  }
}
