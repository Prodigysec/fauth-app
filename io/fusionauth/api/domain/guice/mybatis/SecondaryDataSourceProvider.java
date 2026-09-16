package io.fusionauth.api.domain.guice.mybatis;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.inversoft.configuration.InversoftConfiguration;
import com.inversoft.jdbc.hikari.DataSourceProvider;
import com.zaxxer.hikari.HikariConfig;
import javax.inject.Inject;

public class SecondaryDataSourceProvider extends DataSourceProvider {
  @Inject
  public SecondaryDataSourceProvider(InversoftConfiguration paramInversoftConfiguration, HealthCheckRegistry paramHealthCheckRegistry, MetricRegistry paramMetricRegistry) {
    super(paramInversoftConfiguration, paramHealthCheckRegistry, paramMetricRegistry);
  }
  
  protected void configureHikari(HikariConfig paramHikariConfig) {
    super.configureHikari(paramHikariConfig);
    paramHikariConfig.setMinimumIdle(1);
    paramHikariConfig.setPoolName("Database-" + DataSourceName.secondary.name());
  }
}
