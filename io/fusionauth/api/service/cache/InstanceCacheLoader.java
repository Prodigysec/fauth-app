package io.fusionauth.api.service.cache;

import com.google.inject.Inject;
import com.inversoft.cache.BaseCacheLoader;
import com.inversoft.cache.Cache;
import com.inversoft.license.v2.LicenseProvider;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.InstanceMapper;
import io.fusionauth.api.license.ReactorChangeResult;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceCacheLoader extends BaseCacheLoader<InstanceCache.SingleEntry, Instance> implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(InstanceCacheLoader.class);
  
  private final InstanceCache instanceCache;
  
  private final InstanceMapper instanceMapper;
  
  private final IpReputationLoader ipReputationLoader;
  
  private final LicenseProvider licenseProvider;
  
  private final MaxMindDatabaseLoader maxMindDatabaseLoader;
  
  private final UserAgentReputationLoader userAgentReputationLoader;
  
  @Inject
  public InstanceCacheLoader(InstanceMapper paramInstanceMapper, InstanceCache paramInstanceCache, LicenseProvider paramLicenseProvider, MaxMindDatabaseLoader paramMaxMindDatabaseLoader, IpReputationLoader paramIpReputationLoader, UserAgentReputationLoader paramUserAgentReputationLoader) {
    super(Collections.emptyList());
    this.instanceMapper = paramInstanceMapper;
    this.instanceCache = paramInstanceCache;
    this.licenseProvider = paramLicenseProvider;
    this.maxMindDatabaseLoader = paramMaxMindDatabaseLoader;
    this.ipReputationLoader = paramIpReputationLoader;
    this.userAgentReputationLoader = paramUserAgentReputationLoader;
  }
  
  public void run() {
    load();
  }
  
  protected void internalLoad(Cache<InstanceCache.SingleEntry, Instance> paramCache) {
    Instance instance1 = (Instance)paramCache.get(InstanceCache.SingleEntry.SingleEntry);
    logger.debug("InstanceCacheLoader, existing cache instance of [{}]", instance1);
    Instance instance2 = this.instanceMapper.retrieve();
    if (!IpReputationLoader.isLicensed(this.licenseProvider.getLicense()))
      this.ipReputationLoader.removeCache(); 
    if (!UserAgentReputationLoader.isLicensed(this.licenseProvider.getLicense()))
      this.userAgentReputationLoader.removeCache(); 
    if (!MaxMindDatabaseLoader.isLicensed(this.licenseProvider.getLicense()))
      this.maxMindDatabaseLoader.removeCache(); 
    logger.debug("Replacing instance cache instance with updated instance from database [{}]", instance2);
    Map map = Map.of(InstanceCache.SingleEntry.SingleEntry, instance2);
    this.instanceCache.replace(map);
    if (instance1 != null) {
      ReactorChangeResult reactorChangeResult = ReactorChangeResult.getChanges(instance1.reactorHealthChecks, instance2.reactorHealthChecks);
      if (reactorChangeResult.anyChanges())
        logger.info("Received instance cache update with Reactor status changes"); 
    } 
  }
  
  protected Cache<InstanceCache.SingleEntry, Instance> resolveCache() {
    return (Cache<InstanceCache.SingleEntry, Instance>)this.instanceCache;
  }
}
