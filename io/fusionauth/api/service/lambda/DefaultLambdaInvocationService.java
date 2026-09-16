package io.fusionauth.api.service.lambda;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import io.fusionauth.api.domain.CompiledLambda;
import io.fusionauth.api.domain.api.service.LambdaArgument;
import io.fusionauth.api.service.cache.LambdaCache;
import io.fusionauth.api.service.system.EventLogHelper;
import io.fusionauth.api.service.system.LambdaInvocationException;
import io.fusionauth.domain.EventLog;
import io.fusionauth.domain.EventLogType;
import io.fusionauth.domain.Lambda;
import java.util.UUID;

public class DefaultLambdaInvocationService implements LambdaInvocationService {
  private final LambdaCache lambdaCache;
  
  private final LambdaEngine lambdaEngine;
  
  private final MetricRegistry metricRegistry;
  
  @Inject
  public DefaultLambdaInvocationService(LambdaCache paramLambdaCache, LambdaEngine paramLambdaEngine, MetricRegistry paramMetricRegistry) {
    this.lambdaCache = paramLambdaCache;
    this.lambdaEngine = paramLambdaEngine;
    this.metricRegistry = paramMetricRegistry;
  }
  
  public void invoke(UUID paramUUID, LambdaArgument... paramVarArgs) {
    CompiledLambda.InvocationResult invocationResult;
    CompiledLambda compiledLambda = (CompiledLambda)this.lambdaCache.get(paramUUID);
    if (compiledLambda == null)
      return; 
    Timer timer1 = this.metricRegistry.timer("lambda.[*].invocations");
    Timer timer2 = this.metricRegistry.timer("lambda.[" + String.valueOf(paramUUID) + "].invocations");
    Meter meter1 = this.metricRegistry.meter("lambda.[*].failures");
    Meter meter2 = this.metricRegistry.meter("lambda.[" + String.valueOf(paramUUID) + "].failures");
    try {
      Timer.Context context = timer1.time();
      try {
        Timer.Context context1 = timer2.time();
        try {
          invocationResult = this.lambdaEngine.invoke(compiledLambda, paramVarArgs);
          if (context1 != null)
            context1.close(); 
        } catch (Throwable throwable) {
          if (context1 != null)
            try {
              context1.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
        if (context != null)
          context.close(); 
      } catch (Throwable throwable) {
        if (context != null)
          try {
            context.close();
          } catch (Throwable throwable1) {
            throwable.addSuppressed(throwable1);
          }  
        throw throwable;
      } 
    } catch (Exception exception) {
      meter1.mark();
      meter2.mark();
      EventLogHelper.create(new EventLog(EventLogType.Error, exception(compiledLambda, exception)));
      throw new LambdaInvocationException(exception);
    } 
    if (!invocationResult.info.isEmpty())
      EventLogHelper.create(new EventLog(EventLogType.Information, message(compiledLambda, invocationResult.info))); 
    if (compiledLambda.debug && !invocationResult.debug.isEmpty())
      EventLogHelper.create(new EventLog(EventLogType.Debug, message(compiledLambda, invocationResult.debug))); 
    if (!invocationResult.error.isEmpty())
      EventLogHelper.create(new EventLog(EventLogType.Error, message(compiledLambda, invocationResult.error))); 
    if (invocationResult.exception != null) {
      meter1.mark();
      meter2.mark();
      EventLogHelper.create(new EventLog(EventLogType.Error, exception(compiledLambda, invocationResult.exception)));
      throw new LambdaInvocationException(invocationResult.exception);
    } 
  }
  
  protected String exception(Lambda paramLambda, Exception paramException) {
    return "Lambda invocation exception.\n\nId: " + String.valueOf(paramLambda.id) + "\nName: " + paramLambda.name + "\n\n" + paramException.getMessage() + "\n";
  }
  
  protected String message(Lambda paramLambda, String paramString) {
    return "Lambda invocation result.\n\nId: " + String.valueOf(paramLambda.id) + "\nName: " + paramLambda.name + "\n\n" + paramString;
  }
}
