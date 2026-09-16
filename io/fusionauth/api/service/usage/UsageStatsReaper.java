package io.fusionauth.api.service.usage;

import com.google.inject.Inject;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.api.service.system.ReaperService;
import io.fusionauth.app.service.MasterNodeRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsageStatsReaper extends MasterNodeRunnable {
  private static final Logger logger = LoggerFactory.getLogger(UsageStatsReaper.class);
  
  private final ReaperService reaperService;
  
  @Inject
  public UsageStatsReaper(NodeService paramNodeService, ReaperService paramReaperService) {
    super(paramNodeService);
    this.reaperService = paramReaperService;
  }
  
  public void runScheduled() {
    int i = this.reaperService.reapUsageStats();
    logger.debug("Finish Usage Stats reaper. Deleted [{}] usage stats.", Integer.valueOf(i));
  }
}
