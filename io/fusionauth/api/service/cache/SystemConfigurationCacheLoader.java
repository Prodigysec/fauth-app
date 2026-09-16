package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.BaseSingleValueCacheLoader;
import com.inversoft.cache.SingleValueCache;
import io.fusionauth.api.service.system.SystemConfigurationService;
import io.fusionauth.domain.SystemConfiguration;

public class SystemConfigurationCacheLoader extends BaseSingleValueCacheLoader<SystemConfiguration> implements Runnable {
  private final SystemConfigurationCache cache;
  
  private final SystemConfigurationService systemConfigurationService;
  
  @Inject
  public SystemConfigurationCacheLoader(SystemConfigurationCache paramSystemConfigurationCache, SystemConfigurationService paramSystemConfigurationService) {
    this.cache = paramSystemConfigurationCache;
    this.systemConfigurationService = paramSystemConfigurationService;
  }
  
  public void run() {
    load();
  }
  
  protected void internalLoad(SingleValueCache<SystemConfiguration> paramSingleValueCache) {
    paramSingleValueCache.set(this.systemConfigurationService.retrieve());
  }
  
  protected SingleValueCache<SystemConfiguration> resolveCache() {
    return (SingleValueCache<SystemConfiguration>)this.cache;
  }
}
