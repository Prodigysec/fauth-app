package io.fusionauth.domain.api.tenantManager;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.tenantManager.TenantManagerConfiguration;

public class TenantManagerConfigurationRequest {
  public TenantManagerConfiguration tenantManagerConfiguration;
  
  @JacksonConstructor
  public TenantManagerConfigurationRequest() {}
  
  public TenantManagerConfigurationRequest(TenantManagerConfiguration paramTenantManagerConfiguration) {
    this.tenantManagerConfiguration = paramTenantManagerConfiguration;
  }
}
