package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginRecordReaper implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(LoginRecordReaper.class);
  
  private final NodeService nodeService;
  
  private final ReaperService reaperService;
  
  @Inject
  public LoginRecordReaper(NodeService paramNodeService, ReaperService paramReaperService) {
    this.nodeService = paramNodeService;
    this.reaperService = paramReaperService;
  }
  
  public void run() {
    if (this.nodeService.isMaster()) {
      logger.debug("Start Login Record Log reaper");
      int i = this.reaperService.reapLoginRecords();
      if (i == -1) {
        logger.debug("Delete is not enabled for Login Records, the delete operation was not attempted.");
      } else {
        logger.debug("Finish Login Record reaper. Deleted [{}] login records.", Integer.valueOf(i));
      } 
    } 
  }
}
