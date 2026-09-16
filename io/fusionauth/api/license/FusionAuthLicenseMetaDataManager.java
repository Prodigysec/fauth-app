package io.fusionauth.api.license;

import com.google.inject.Inject;
import com.inversoft.cache.CacheNotifier;
import com.inversoft.configuration.InversoftConfiguration;
import com.inversoft.license.v2.BaseConfigurationLicenseMetaDataManager;
import com.inversoft.license.v2.domain.LicenseMetaData;
import com.inversoft.support.service.guice.ProductVersionString;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.InstanceMapper;
import java.util.Map;
import org.mybatis.guice.transactional.Transactional;

public class FusionAuthLicenseMetaDataManager extends BaseConfigurationLicenseMetaDataManager {
  private final CacheNotifier cacheNotifier;
  
  private final InstanceMapper instanceMapper;
  
  private final String productVersion;
  
  @Inject
  public FusionAuthLicenseMetaDataManager(InversoftConfiguration paramInversoftConfiguration, InstanceMapper paramInstanceMapper, CacheNotifier paramCacheNotifier, @ProductVersionString String paramString) {
    super(paramInversoftConfiguration);
    this.instanceMapper = paramInstanceMapper;
    this.cacheNotifier = paramCacheNotifier;
    this.productVersion = paramString;
  }
  
  @Transactional
  public void update(LicenseMetaData paramLicenseMetaData) {
    this.instanceMapper.updateLicenseId(paramLicenseMetaData.id);
    this.cacheNotifier.reload("Instance");
  }
  
  protected Map<String, String> localData() {
    Instance instance = this.instanceMapper.retrieve();
    return Map.of("instanceId", instance.id.toString(), "version", this.productVersion);
  }
  
  protected String localLookup() {
    return this.instanceMapper.retrieveLicenseId();
  }
}
