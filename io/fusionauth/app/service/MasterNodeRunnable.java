package io.fusionauth.app.service;

import io.fusionauth.api.service.system.NodeService;

public abstract class MasterNodeRunnable implements Runnable {
  protected final NodeService nodeService;
  
  protected MasterNodeRunnable(NodeService paramNodeService) {
    this.nodeService = paramNodeService;
  }
  
  public final void run() {
    if (shouldRun() && this.nodeService.isMaster())
      runScheduled(); 
  }
  
  public abstract void runScheduled();
  
  protected boolean shouldRun() {
    return true;
  }
}
