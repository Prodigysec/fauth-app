package io.fusionauth.api.service.event;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.fusionauth.api.service.email.FusionAuthInstrumentedExecutorService;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventExecutorProvider implements Provider<ExecutorService>, Closeable {
  private static final Logger logger = LoggerFactory.getLogger(EventExecutorProvider.class);
  
  private final MetricRegistry metricRegistry;
  
  private ExecutorService executorService;
  
  @Inject
  public EventExecutorProvider(MetricRegistry paramMetricRegistry) {
    this.metricRegistry = paramMetricRegistry;
  }
  
  public void close() throws IOException {
    logger.info("Shutting down the Event executor service.");
    this.executorService.shutdownNow();
  }
  
  public ExecutorService get() {
    if (this.executorService == null)
      this.executorService = (ExecutorService)new FusionAuthInstrumentedExecutorService(Executors.newCachedThreadPool(paramRunnable -> {
              Thread thread = new Thread(paramRunnable, "EventExecutorService Send Thread");
              thread.setDaemon(true);
              return thread;
            }), this.metricRegistry, "event.executor.tasks"); 
    return this.executorService;
  }
}
