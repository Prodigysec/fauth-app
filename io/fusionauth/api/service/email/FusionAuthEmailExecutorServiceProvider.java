package io.fusionauth.api.service.email;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import org.primeframework.email.service.EmailExecutorServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FusionAuthEmailExecutorServiceProvider extends EmailExecutorServiceProvider implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(FusionAuthEmailExecutorServiceProvider.class);
  
  private final MetricRegistry metricRegistry;
  
  private ExecutorService executorService;
  
  @Inject
  public FusionAuthEmailExecutorServiceProvider(MetricRegistry paramMetricRegistry) {
    this.metricRegistry = paramMetricRegistry;
  }
  
  public void close() throws IOException {
    logger.info("Shutting down the Email executor service.");
    this.executorService.shutdownNow();
  }
  
  public ExecutorService get() {
    if (this.executorService == null)
      this.executorService = (ExecutorService)new FusionAuthInstrumentedExecutorService(super.get(), this.metricRegistry, "email.executor.tasks"); 
    return this.executorService;
  }
  
  protected String threadName() {
    return "Email Executor Thread";
  }
}
