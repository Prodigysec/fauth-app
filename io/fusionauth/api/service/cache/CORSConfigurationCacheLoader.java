package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.BaseSingleValueCacheLoader;
import com.inversoft.cache.SingleValueCache;
import io.fusionauth.api.domain.SystemConfigurationMapper;
import io.fusionauth.domain.CORSConfiguration;

public class CORSConfigurationCacheLoader extends BaseSingleValueCacheLoader<CORSConfiguration> implements Runnable {
  private final CORSConfigurationCache cache;
  
  private final SystemConfigurationMapper systemConfigurationMapper;
  
  @Inject
  public CORSConfigurationCacheLoader(CORSConfigurationCache paramCORSConfigurationCache, SystemConfigurationMapper paramSystemConfigurationMapper) {
    this.cache = paramCORSConfigurationCache;
    this.systemConfigurationMapper = paramSystemConfigurationMapper;
  }
  
  public void run() {
    load();
  }
  
  protected void internalLoad(SingleValueCache<CORSConfiguration> paramSingleValueCache) {
    CORSConfiguration cORSConfiguration = (this.systemConfigurationMapper.retrieve()).corsConfiguration;
    paramSingleValueCache.set(cORSConfiguration);
  }
  
  protected SingleValueCache<CORSConfiguration> resolveCache() {
    return (SingleValueCache<CORSConfiguration>)this.cache;
  }
}
