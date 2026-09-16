package io.fusionauth.api.metrics.prometheus;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import io.prometheus.client.dropwizard.DropwizardExports;

public final class PrometheusCollectorRegistrar {
  @Inject
  public PrometheusCollectorRegistrar(MetricRegistry paramMetricRegistry) {
    (new DropwizardExports(paramMetricRegistry)).register();
  }
}
