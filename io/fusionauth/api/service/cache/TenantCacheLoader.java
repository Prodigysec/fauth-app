package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.BaseCacheLoader;
import com.inversoft.cache.Cache;
import io.fusionauth.api.service.system.TenantReaderService;
import io.fusionauth.domain.Tenant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TenantCacheLoader extends BaseCacheLoader<UUID, Tenant> implements Runnable {
  private final TenantCache cache;
  
  private final TenantReaderService tenantReader;
  
  @Inject
  public TenantCacheLoader(TenantCache paramTenantCache, TenantReaderService paramTenantReaderService) {
    super(Collections.emptyList());
    this.tenantReader = paramTenantReaderService;
    this.cache = paramTenantCache;
  }
  
  public void run() {
    load();
  }
  
  protected void internalLoad(Cache<UUID, Tenant> paramCache) {
    ((TenantCache)paramCache).setDefaultTenantId(this.tenantReader.retrieveFusionAuthTenantId());
    paramCache.replace((Map)this.tenantReader.retrieveAll().stream().collect(Collectors.toMap(paramTenant -> paramTenant.id, paramTenant -> paramTenant)));
  }
  
  protected Cache<UUID, Tenant> resolveCache() {
    return (Cache<UUID, Tenant>)this.cache;
  }
}
