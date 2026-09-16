package io.fusionauth.api.service.jwt;

import com.google.inject.Inject;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.app.service.MasterNodeRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefreshTokenReaper extends MasterNodeRunnable {
  private static final Logger logger = LoggerFactory.getLogger(RefreshTokenReaper.class);
  
  private final RefreshTokenService refreshTokenService;
  
  @Inject
  public RefreshTokenReaper(NodeService paramNodeService, RefreshTokenService paramRefreshTokenService) {
    super(paramNodeService);
    this.refreshTokenService = paramRefreshTokenService;
  }
  
  public void runScheduled() {
    long l1 = System.currentTimeMillis();
    int i = this.refreshTokenService.revokeExpiredRefreshTokens();
    long l2 = System.currentTimeMillis();
    logger.debug("Cleared [{}] expired refresh tokens. Completed in {} milliseconds.", Integer.valueOf(i), Long.valueOf(l2 - l1));
  }
}
