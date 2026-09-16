package io.fusionauth.api.service.reactor;

import com.google.inject.Inject;
import io.fusionauth.api.service.system.NodeService;

public class CommonPasswordLoader implements Runnable {
  private final ReactorService reactorService;
  
  private final NodeService nodeService;
  
  @Inject
  public CommonPasswordLoader(ReactorService paramReactorService, NodeService paramNodeService) {
    this.reactorService = paramReactorService;
    this.nodeService = paramNodeService;
  }
  
  public void run() {
    if (this.nodeService.isMaster())
      this.reactorService.updateCommonPasswordDataset(); 
  }
}
