package io.fusionauth.api.service.metrics;

import com.google.inject.Inject;
import com.inversoft.rest.ClientResponse;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.InstanceMapper;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.api.service.system.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsLogger implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(MetricsLogger.class);
  
  private final FusionAuthConfiguration configuration;
  
  private final InstanceMapper instanceMapper;
  
  private final NodeService nodeService;
  
  private final ReactorService reactorService;
  
  private final MetricsSender sender;
  
  @Inject
  public MetricsLogger(FusionAuthConfiguration paramFusionAuthConfiguration, InstanceMapper paramInstanceMapper, NodeService paramNodeService, ReactorService paramReactorService, MetricsSender paramMetricsSender) {
    this.configuration = paramFusionAuthConfiguration;
    this.instanceMapper = paramInstanceMapper;
    this.reactorService = paramReactorService;
    this.nodeService = paramNodeService;
    this.sender = paramMetricsSender;
  }
  
  public void run() {
    if (this.configuration.runtimeMode() != RuntimeMode.Development && this.configuration.runtimeMode() != RuntimeMode.Production)
      return; 
    Instance instance = this.instanceMapper.retrieve();
    if (!instance.setupComplete)
      return; 
    if (this.nodeService.isMaster())
      sendMetrics(instance); 
  }
  
  public void sendMetrics(Instance paramInstance) {
    ClientResponse<Void, Void> clientResponse = this.sender.send(paramInstance);
    if (!clientResponse.wasSuccessful()) {
      if (clientResponse.exception != null) {
        logger.debug("Unable to call the metrics endpoint.", clientResponse.exception);
      } else {
        logger.debug("Unable to call metrics - status [{}].", Integer.valueOf(clientResponse.status));
      } 
    } else if (clientResponse.status == 205) {
      this.reactorService.deactivate();
    } 
  }
}
