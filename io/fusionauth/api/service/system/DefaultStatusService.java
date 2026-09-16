package io.fusionauth.api.service.system;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.inject.Inject;
import com.inversoft.metrics.HealthCheckResult;
import com.inversoft.metrics.Metrics;
import com.inversoft.support.service.guice.ProductVersionString;
import java.time.temporal.ChronoUnit;
import java.util.TreeMap;

public class DefaultStatusService implements StatusService {
  private final HealthCheckRegistry healthCheckRegistry;
  
  private final MetricRegistry metricRegistry;
  
  private final String productVersion;
  
  @Inject
  public DefaultStatusService(HealthCheckRegistry paramHealthCheckRegistry, MetricRegistry paramMetricRegistry, @ProductVersionString String paramString) {
    this.healthCheckRegistry = paramHealthCheckRegistry;
    this.metricRegistry = paramMetricRegistry;
    this.productVersion = paramString;
  }
  
  public StatusService.StatusResponse get() {
    StatusService.StatusResponse statusResponse = new StatusService.StatusResponse();
    statusResponse.healthChecks = new TreeMap<>();
    this.healthCheckRegistry.runHealthChecks().forEach((paramString, paramResult) -> paramStatusResponse.healthChecks.put(paramString, new HealthCheckResult(paramResult)));
    statusResponse.metrics = new Metrics(this.metricRegistry, ChronoUnit.MILLIS);
    statusResponse.version = this.productVersion;
    return statusResponse;
  }
  
  public HealthCheckResult runHealthCheck(String paramString) {
    return new HealthCheckResult(this.healthCheckRegistry.runHealthCheck(paramString));
  }
}
