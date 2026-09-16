package io.fusionauth.api.service.cache;

import com.inversoft.cache.SimpleSingleValueCache;
import io.fusionauth.domain.SystemConfiguration;

public class SystemConfigurationCache extends SimpleSingleValueCache<SystemConfiguration> {
  public SystemConfiguration get() {
    return new SystemConfiguration((SystemConfiguration)super.get());
  }
}
