package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Tenant;
import java.util.List;

public class TenantResponse {
  public Tenant tenant;
  
  public List<Tenant> tenants;
  
  public TenantResponse(List<Tenant> paramList) {
    this.tenants = paramList;
  }
  
  public TenantResponse(Tenant paramTenant) {
    this.tenant = paramTenant;
  }
  
  @JacksonConstructor
  public TenantResponse() {}
}
