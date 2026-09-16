package io.fusionauth.api.service.user;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.domain.RequestFrequencyMapper;
import io.fusionauth.api.domain.TenantMapper;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.api.service.tenant.RateLimitHelper;
import io.fusionauth.app.service.MasterNodeRunnable;
import io.fusionauth.domain.RateLimitedRequestType;
import io.fusionauth.domain.Tenant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestFrequencyRecordReaper extends MasterNodeRunnable {
  private static final Logger logger = LoggerFactory.getLogger(RequestFrequencyRecordReaper.class);
  
  private final RequestFrequencyMapper backgroundRequestFrequencyMapper;
  
  private final TenantMapper backgroundTenantMapper;
  
  @Inject
  public RequestFrequencyRecordReaper(NodeService paramNodeService, @Named("background") RequestFrequencyMapper paramRequestFrequencyMapper, @Named("background") TenantMapper paramTenantMapper) {
    super(paramNodeService);
    this.backgroundRequestFrequencyMapper = paramRequestFrequencyMapper;
    this.backgroundTenantMapper = paramTenantMapper;
  }
  
  public void runScheduled() {
    long l = System.currentTimeMillis();
    List<Tenant> list = this.backgroundTenantMapper.retrieveAll();
    for (RateLimitedRequestType rateLimitedRequestType : RateLimitedRequestType.values()) {
      int i = 0;
      for (Tenant tenant : list) {
        boolean bool = (RateLimitHelper.getConfiguration(tenant.rateLimitConfiguration, rateLimitedRequestType)).enabled;
        boolean bool1 = (rateLimitedRequestType == RateLimitedRequestType.FailedLogin && tenant.failedAuthenticationConfiguration.userActionId != null) ? true : false;
        if (bool || bool1) {
          long l2 = l - TimeUnit.SECONDS.toMillis(bool1 ? 
              tenant.failedAuthenticationConfiguration.resetCountInSeconds : 
              (RateLimitHelper.getConfiguration(tenant.rateLimitConfiguration, rateLimitedRequestType)).timePeriodInSeconds);
          i += this.backgroundRequestFrequencyMapper.deleteRequestFrequencyRecordsOlderThan(tenant.id, rateLimitedRequestType, l2);
          continue;
        } 
        i += this.backgroundRequestFrequencyMapper.deleteRequestFrequencyRecordsOlderThan(tenant.id, rateLimitedRequestType, l);
      } 
      long l1 = System.currentTimeMillis();
      logger.debug("Cleared [{}] old rate limited records of type [{}]. Completed in {} milliseconds.", new Object[] { Integer.valueOf(i), rateLimitedRequestType, Long.valueOf(l1 - l) });
    } 
  }
}
