package io.fusionauth.api.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.inversoft.metrics.guice.MetricsModule;
import io.fusionauth.api.metrics.prometheus.PrometheusCollectorRegistrar;
import io.prometheus.client.CollectorRegistry;

public class FusionAuthMetricModule extends MetricsModule {
  protected void configure() {
    super.configure();
    bind(CollectorRegistry.class).toInstance(CollectorRegistry.defaultRegistry);
    bind(PrometheusCollectorRegistrar.class).asEagerSingleton();
    bind(FusionAuthJVMStatsBinder.class).asEagerSingleton();
  }
  
  protected void configureHealthCheckRegistry(HealthCheckRegistry paramHealthCheckRegistry) {
    super.configureHealthCheckRegistry(paramHealthCheckRegistry);
    bind(FusionHealthCheckBinder.class).asEagerSingleton();
  }
}
