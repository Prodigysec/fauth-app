package io.fusionauth.api.service.system;

import com.google.inject.Inject;

public class NodeManager implements Runnable {
  private final NodeService nodeService;
  
  @Inject
  public NodeManager(NodeService paramNodeService) {
    this.nodeService = paramNodeService;
  }
  
  public void run() {
    this.nodeService.update();
  }
}
