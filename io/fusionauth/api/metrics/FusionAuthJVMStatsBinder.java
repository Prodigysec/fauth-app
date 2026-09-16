package io.fusionauth.api.metrics;

import com.codahale.metrics.JvmAttributeGaugeSet;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.google.inject.Inject;
import java.lang.management.ManagementFactory;

public class FusionAuthJVMStatsBinder {
  @Inject
  public FusionAuthJVMStatsBinder(MetricRegistry paramMetricRegistry) {
    paramMetricRegistry.register("jvm.buffers", (Metric)new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
    paramMetricRegistry.register("jvm.classLoading", (Metric)new ClassLoadingGaugeSet());
    paramMetricRegistry.register("jvm.garbageCollection", (Metric)new GarbageCollectorMetricSet());
    paramMetricRegistry.register("jvm.memory", (Metric)new MemoryUsageGaugeSet());
    paramMetricRegistry.register("jvm.threads", (Metric)new ThreadStatesGaugeSet());
    paramMetricRegistry.register("jvm.attributes", (Metric)new JvmAttributeGaugeSet());
  }
}
