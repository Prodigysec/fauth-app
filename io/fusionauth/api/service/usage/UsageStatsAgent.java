package io.fusionauth.api.service.usage;

import com.google.inject.name.Named;
import io.fusionauth.api.configuration.FusionAuthConfiguration;
import io.fusionauth.api.domain.CollectedUsageStats;
import io.fusionauth.api.domain.Instance;
import io.fusionauth.api.domain.InstanceMapper;
import io.fusionauth.api.domain.RuntimeMode;
import io.fusionauth.api.domain.SavedCurrentStats;
import io.fusionauth.api.domain.UsageStatsMapper;
import io.fusionauth.api.domain.guice.mybatis.UseDataSource;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.api.service.system.SystemConfigurationService;
import io.fusionauth.app.service.MasterNodeRunnable;
import io.fusionauth.usagestats.shared.domain.CurrentStats;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsageStatsAgent extends MasterNodeRunnable {
  public static final int AvgDaysInMonth = 30;
  
  private static final Logger logger = LoggerFactory.getLogger(UsageStatsAgent.class);
  
  private final Set<RuntimeMode> RunnableRuntimeModes = Set.of(RuntimeMode.Development, RuntimeMode.Production, RuntimeMode.FusionAuth_Development);
  
  private final FusionAuthConfiguration configuration;
  
  private final InstanceMapper instanceMapper;
  
  private final SystemConfigurationService systemConfigurationService;
  
  private final UsageStatsClient usageStatsClient;
  
  private final UsageStatsMapper usageStatsMapper;
  
  @Inject
  public UsageStatsAgent(FusionAuthConfiguration paramFusionAuthConfiguration, @Named("background") InstanceMapper paramInstanceMapper, NodeService paramNodeService, @Named("background") UsageStatsMapper paramUsageStatsMapper, UsageStatsClient paramUsageStatsClient, SystemConfigurationService paramSystemConfigurationService) {
    super(paramNodeService);
    this.configuration = paramFusionAuthConfiguration;
    this.instanceMapper = paramInstanceMapper;
    this.systemConfigurationService = paramSystemConfigurationService;
    this.usageStatsClient = paramUsageStatsClient;
    this.usageStatsMapper = paramUsageStatsMapper;
  }
  
  public void refreshCurrentStats() {
    ZonedDateTime zonedDateTime1 = this.usageStatsClient.fetchStatsLastModified();
    if (zonedDateTime1 == null)
      return; 
    ZonedDateTime zonedDateTime2 = this.usageStatsMapper.retrieveCurrentStatsLastModified();
    if (zonedDateTime2 != null && !zonedDateTime2.truncatedTo(ChronoUnit.SECONDS).isBefore(zonedDateTime1.truncatedTo(ChronoUnit.SECONDS))) {
      this.usageStatsMapper.updateLastCheckedCurrentStats(ZonedDateTime.now(ZoneOffset.UTC));
    } else {
      updateCurrentStatsForCollection();
    } 
  }
  
  public void runScheduled() {
    refreshCurrentStats();
    sendUnsentStats();
  }
  
  public void sendUnsentStats() {
    List<CollectedUsageStats> list = this.usageStatsMapper.retrieveUnsent(30);
    for (CollectedUsageStats collectedUsageStats : list) {
      if (this.usageStatsClient.sendStat(collectedUsageStats))
        this.usageStatsMapper.markUsageStatSent(collectedUsageStats.collectionInstant); 
    } 
  }
  
  protected boolean shouldRun() {
    Instance instance = this.instanceMapper.retrieve();
    return (this.RunnableRuntimeModes.contains(this.configuration.runtimeMode()) && instance.setupComplete && this.systemConfigurationService
      
      .isUsageStatsEnabled());
  }
  
  @Transactional
  @UseDataSource("background")
  private void updateCurrentStatsForCollection() {
    logger.debug("The usage stats list has been updated. Fetching new stats.");
    CurrentStats currentStats = this.usageStatsClient.fetchCurrentStats();
    if (currentStats != null) {
      this.usageStatsMapper.deleteAllCurrentUsageStats();
      this.usageStatsMapper.saveCurrentUsageStats(new SavedCurrentStats(currentStats, ZonedDateTime.now(ZoneOffset.UTC)));
    } 
  }
}
