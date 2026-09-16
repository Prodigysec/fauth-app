package io.fusionauth.api.domain.guice.mybatis;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.inversoft.configuration.InversoftConfiguration;
import com.inversoft.jdbc.hikari.DataSourceProvider;
import com.zaxxer.hikari.HikariConfig;
import javax.inject.Inject;

public class PrimaryDataSourceProvider extends DataSourceProvider {
  @Inject
  public PrimaryDataSourceProvider(InversoftConfiguration paramInversoftConfiguration, HealthCheckRegistry paramHealthCheckRegistry, MetricRegistry paramMetricRegistry) {
    super(paramInversoftConfiguration, paramHealthCheckRegistry, paramMetricRegistry);
  }
  
  protected void configureHikari(HikariConfig paramHikariConfig) {
    super.configureHikari(paramHikariConfig);
    paramHikariConfig.setPoolName("Database-" + DataSourceName.primary.name());
  }
}
