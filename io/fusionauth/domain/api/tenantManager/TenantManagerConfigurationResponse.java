package io.fusionauth.domain.api.tenantManager;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.tenantManager.TenantManagerConfiguration;

public class TenantManagerConfigurationResponse {
  public TenantManagerConfiguration tenantManagerConfiguration;
  
  @JacksonConstructor
  public TenantManagerConfigurationResponse() {}
  
  public TenantManagerConfigurationResponse(TenantManagerConfiguration paramTenantManagerConfiguration) {
    this.tenantManagerConfiguration = paramTenantManagerConfiguration;
  }
}
