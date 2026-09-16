package io.fusionauth.api.service.cache;

import com.inversoft.cache.SimpleCache;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.Tenantable;
import java.util.UUID;
import java.util.function.Function;

public class TenantCache extends SimpleCache<UUID, Tenant> {
  private UUID defaultTenantId;
  
  public Tenant get(UUID paramUUID, Function<UUID, Tenant> paramFunction) {
    Tenant tenant = (Tenant)get(paramUUID);
    if (tenant == null && paramFunction != null) {
      tenant = paramFunction.apply(paramUUID);
      if (tenant != null)
        set(paramUUID, tenant); 
    } 
    return (tenant != null) ? new Tenant(tenant) : null;
  }
  
  public Tenant getDefaultOrNull() {
    return getDefaultOrNull((Function<UUID, Tenant>)null);
  }
  
  public Tenant getDefaultOrNull(Function<UUID, Tenant> paramFunction) {
    if (this.cache.size() > 1)
      return null; 
    return get(this.defaultTenantId, paramFunction);
  }
  
  public UUID getDefaultTenantId() {
    return this.defaultTenantId;
  }
  
  public void setDefaultTenantId(UUID paramUUID) {
    this.defaultTenantId = paramUUID;
  }
  
  public Tenant resolve(Tenant paramTenant, Tenantable... paramVarArgs) {
    return resolve(paramTenant, (Function<UUID, Tenant>)null, paramVarArgs);
  }
  
  public Tenant resolve(Tenant paramTenant, Function<UUID, Tenant> paramFunction, Tenantable... paramVarArgs) {
    if (paramTenant != null)
      return paramTenant; 
    for (Tenantable tenantable : paramVarArgs) {
      if (tenantable != null) {
        Tenant tenant = (paramFunction != null) ? get(tenantable.getTenantId(), paramFunction) : (Tenant)get(tenantable.getTenantId());
        if (tenant != null)
          return tenant; 
      } 
    } 
    return null;
  }
}
