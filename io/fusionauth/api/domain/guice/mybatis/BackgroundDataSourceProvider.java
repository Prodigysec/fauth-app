package io.fusionauth.api.domain.guice.mybatis;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.inversoft.configuration.InversoftConfiguration;
import com.inversoft.jdbc.hikari.DataSourceProvider;
import com.zaxxer.hikari.HikariConfig;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

public class BackgroundDataSourceProvider extends DataSourceProvider {
  @Inject
  public BackgroundDataSourceProvider(InversoftConfiguration paramInversoftConfiguration, HealthCheckRegistry paramHealthCheckRegistry, MetricRegistry paramMetricRegistry) {
    super(paramInversoftConfiguration, paramHealthCheckRegistry, paramMetricRegistry);
  }
  
  protected void configureHikari(HikariConfig paramHikariConfig) {
    super.configureHikari(paramHikariConfig);
    paramHikariConfig.setConnectionTimeout(TimeUnit.SECONDS.toMillis(30L));
    paramHikariConfig.setMinimumIdle(0);
    paramHikariConfig.setMaximumPoolSize(10);
    paramHikariConfig.setPoolName("Database-" + DataSourceName.background.name());
  }
}
