package io.fusionauth.api.service.reactor;

import com.google.inject.Inject;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.InstanceMapper;
import io.fusionauth.api.service.system.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReactorUpdater implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(ReactorUpdater.class);
  
  private final InstanceMapper instanceMapper;
  
  private final NodeService nodeService;
  
  private final ReactorService reactorService;
  
  @Inject
  public ReactorUpdater(InstanceMapper paramInstanceMapper, ReactorService paramReactorService, NodeService paramNodeService) {
    this.instanceMapper = paramInstanceMapper;
    this.reactorService = paramReactorService;
    this.nodeService = paramNodeService;
  }
  
  public void run() {
    if (this.nodeService.isMaster()) {
      logger.debug("Running ReactorService.updateStatusWithHealthCheck");
      this.reactorService.updateStatusWithHealthCheck();
      logger.debug("ReactorService.updateStatusWithHealthCheck complete");
      return;
    } 
    Instance instance = this.instanceMapper.retrieve();
    if (instance.reactorHealthChecks.isOutOfDate()) {
      logger.info("Possible Reactor field status count has changed, running updateStatusWithHealthCheck even though we are not the master node.");
      this.reactorService.updateStatusWithHealthCheck();
    } 
  }
}
