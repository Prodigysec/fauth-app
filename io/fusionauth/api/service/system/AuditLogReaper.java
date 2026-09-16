package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditLogReaper implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(AuditLogReaper.class);
  
  private final NodeService nodeService;
  
  private final ReaperService reaperService;
  
  @Inject
  public AuditLogReaper(NodeService paramNodeService, ReaperService paramReaperService) {
    this.nodeService = paramNodeService;
    this.reaperService = paramReaperService;
  }
  
  public void run() {
    if (this.nodeService.isMaster()) {
      logger.debug("Start Audit Log reaper");
      int i = this.reaperService.reapAuditLogs();
      if (i == -1) {
        logger.debug("Delete is not enabled for Audit Logs, the delete operation was not attempted.");
      } else {
        logger.debug("Finish Audit Log reaper. Deleted [{}] audit logs.", Integer.valueOf(i));
      } 
    } 
  }
}
