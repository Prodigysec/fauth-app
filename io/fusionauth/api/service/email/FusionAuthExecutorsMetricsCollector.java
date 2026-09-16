package io.fusionauth.api.service.email;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class FusionAuthExecutorsMetricsCollector implements Runnable {
  private final FusionAuthInstrumentedExecutorService emailExecutorService;
  
  private final FusionAuthInstrumentedExecutorService eventExecutorService;
  
  private final MetricRegistry metricRegistry;
  
  @Inject
  public FusionAuthExecutorsMetricsCollector(@Named("EmailExecutorService") ExecutorService paramExecutorService1, @Named("EventExecutorService") ExecutorService paramExecutorService2, MetricRegistry paramMetricRegistry) {
    this.emailExecutorService = (FusionAuthInstrumentedExecutorService)paramExecutorService1;
    this.eventExecutorService = (FusionAuthInstrumentedExecutorService)paramExecutorService2;
    this.metricRegistry = paramMetricRegistry;
  }
  
  public void run() {
    updateMetrics(this.emailExecutorService.getExecutorService(), "email.executor");
    updateMetrics(this.eventExecutorService.getExecutorService(), "event.executor");
  }
  
  private void updateMetrics(ThreadPoolExecutor paramThreadPoolExecutor, String paramString) {
    this.metricRegistry.gauge(MetricRegistry.name(paramString, new String[] { "tasks", "queued" }), () -> ());
    this.metricRegistry.gauge(MetricRegistry.name(paramString, new String[] { "pool", "size", "current" }), () -> {
          Objects.requireNonNull(paramThreadPoolExecutor);
          return paramThreadPoolExecutor::getPoolSize;
        });
    this.metricRegistry.gauge(MetricRegistry.name(paramString, new String[] { "pool", "size", "largest" }), () -> {
          Objects.requireNonNull(paramThreadPoolExecutor);
          return paramThreadPoolExecutor::getLargestPoolSize;
        });
    this.metricRegistry.gauge(MetricRegistry.name(paramString, new String[] { "pool", "size", "min" }), () -> {
          Objects.requireNonNull(paramThreadPoolExecutor);
          return paramThreadPoolExecutor::getCorePoolSize;
        });
    this.metricRegistry.gauge(MetricRegistry.name(paramString, new String[] { "pool", "size", "max" }), () -> {
          Objects.requireNonNull(paramThreadPoolExecutor);
          return paramThreadPoolExecutor::getMaximumPoolSize;
        });
  }
}
