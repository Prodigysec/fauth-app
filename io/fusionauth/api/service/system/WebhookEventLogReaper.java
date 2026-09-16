package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebhookEventLogReaper implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(WebhookEventLogReaper.class);
  
  private final NodeService nodeService;
  
  private final ReaperService reaperService;
  
  @Inject
  public WebhookEventLogReaper(NodeService paramNodeService, ReaperService paramReaperService) {
    this.nodeService = paramNodeService;
    this.reaperService = paramReaperService;
  }
  
  public void run() {
    if (this.nodeService.isMaster()) {
      logger.debug("Start Webhook Event Log reaper");
      int i = this.reaperService.reapWebhookEventLogs();
      logger.debug("Finish Webhook Event Log reaper. Deleted [{}] webhook event logs and associated attempts.", Integer.valueOf(i));
    } 
  }
}
