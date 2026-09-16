package io.fusionauth.api.metrics;

import com.codahale.metrics.health.HealthCheck;
import com.inversoft.configuration.InversoftConfiguration;
import io.fusionauth.api.domain.VersionMapper;

public class DatabaseHealthCheck extends HealthCheck {
  private final InversoftConfiguration configuration;
  
  private final VersionMapper versionMapper;
  
  public DatabaseHealthCheck(InversoftConfiguration paramInversoftConfiguration, VersionMapper paramVersionMapper) {
    this.configuration = paramInversoftConfiguration;
    this.versionMapper = paramVersionMapper;
  }
  
  protected HealthCheck.Result check() {
    try {
      String str = this.versionMapper.retrieveDatabaseVersion();
      return HealthCheck.Result.healthy("Schema version : %s", new Object[] { str });
    } catch (Exception exception) {
      return HealthCheck.Result.unhealthy("Failed to connect to : %s. Exception message: %s", new Object[] { this.configuration.databaseURL(), exception.getMessage() });
    } 
  }
}
