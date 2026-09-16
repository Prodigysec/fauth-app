package io.fusionauth.api.service.email;

import com.codahale.metrics.InstrumentedExecutorService;
import com.codahale.metrics.MetricRegistry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class FusionAuthInstrumentedExecutorService extends InstrumentedExecutorService {
  private final ExecutorService executorService;
  
  public FusionAuthInstrumentedExecutorService(ExecutorService paramExecutorService, MetricRegistry paramMetricRegistry, String paramString) {
    super(paramExecutorService, paramMetricRegistry, paramString);
    this.executorService = paramExecutorService;
  }
  
  public ThreadPoolExecutor getExecutorService() {
    return (ThreadPoolExecutor)this.executorService;
  }
}
