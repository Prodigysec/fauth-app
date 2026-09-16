package io.fusionauth.api.service.usage;

import io.fusionauth.api.domain.CollectedUsageStats;
import io.fusionauth.usagestats.shared.domain.CurrentStats;
import java.time.ZonedDateTime;

public interface UsageStatsClient {
  CurrentStats fetchCurrentStats();
  
  ZonedDateTime fetchStatsLastModified();
  
  boolean sendStat(CollectedUsageStats paramCollectedUsageStats);
}
