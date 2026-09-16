package io.fusionauth.api.service.system;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.domain.AuditLogMapper;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.domain.SystemConfigurationMapper;
import io.fusionauth.api.domain.UsageStatsMapper;
import io.fusionauth.api.domain.WebhookAttemptLogMapper;
import io.fusionauth.api.domain.WebhookEventLogMapper;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.domain.SystemConfiguration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultReaperService implements ReaperService {
  private static final Logger logger = LoggerFactory.getLogger(DefaultReaperService.class);
  
  private final AuditLogMapper backgroundAuditLogMapper;
  
  private final LoginMapper backgroundLoginMapper;
  
  private final SystemConfigurationMapper backgroundSystemConfigurationMapper;
  
  private final WebhookAttemptLogMapper backgroundWebhookAttemptLogMapper;
  
  private final WebhookEventLogMapper backgroundWebhookEventLogMapper;
  
  private final UsageStatsMapper usageStatsMapper;
  
  @Inject
  public DefaultReaperService(@Named("background") AuditLogMapper paramAuditLogMapper, @Named("background") LoginMapper paramLoginMapper, @Named("background") SystemConfigurationMapper paramSystemConfigurationMapper, @Named("background") WebhookAttemptLogMapper paramWebhookAttemptLogMapper, @Named("background") WebhookEventLogMapper paramWebhookEventLogMapper, @Named("background") UsageStatsMapper paramUsageStatsMapper) {
    this.backgroundAuditLogMapper = paramAuditLogMapper;
    this.backgroundLoginMapper = paramLoginMapper;
    this.backgroundSystemConfigurationMapper = paramSystemConfigurationMapper;
    this.backgroundWebhookAttemptLogMapper = paramWebhookAttemptLogMapper;
    this.backgroundWebhookEventLogMapper = paramWebhookEventLogMapper;
    this.usageStatsMapper = paramUsageStatsMapper;
  }
  
  public int reapAuditLogs() {
    SystemConfiguration.DeleteConfiguration deleteConfiguration = (this.backgroundSystemConfigurationMapper.retrieve()).auditLogConfiguration.delete;
    if (deleteConfiguration.enabled) {
      Objects.requireNonNull(this.backgroundAuditLogMapper);
      Objects.requireNonNull(this.backgroundAuditLogMapper);
      return reapOlderThanDays(deleteConfiguration.numberOfDaysToRetain, 2000, this.backgroundAuditLogMapper::retrieveEndOffsetTime, this.backgroundAuditLogMapper::deleteOlderThan, "AuditLog");
    } 
    return -1;
  }
  
  public int reapLoginRecords() {
    SystemConfiguration.DeleteConfiguration deleteConfiguration = (this.backgroundSystemConfigurationMapper.retrieve()).loginRecordConfiguration.delete;
    if (deleteConfiguration.enabled) {
      Objects.requireNonNull(this.backgroundLoginMapper);
      Objects.requireNonNull(this.backgroundLoginMapper);
      return reapOlderThanDays(deleteConfiguration.numberOfDaysToRetain, 10000, this.backgroundLoginMapper::retrieveEndOffsetTime, this.backgroundLoginMapper::deleteOlderThan, "LoginRecord");
    } 
    return -1;
  }
  
  public int reapUsageStats() {
    Objects.requireNonNull(this.usageStatsMapper);
    Objects.requireNonNull(this.usageStatsMapper);
    return reapOlderThanDays((this.backgroundSystemConfigurationMapper.retrieve()).usageDataConfiguration.numberOfDaysToRetain, 10000, this.usageStatsMapper::retrieveEndOffsetTime, this.usageStatsMapper::deleteOlderThan, "UsageStats");
  }
  
  public int reapWebhookEventLogs() {
    SystemConfiguration.DeleteConfiguration deleteConfiguration = (this.backgroundSystemConfigurationMapper.retrieve()).webhookEventLogConfiguration.delete;
    if (deleteConfiguration.enabled) {
      ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC).minusDays(deleteConfiguration.numberOfDaysToRetain);
      int i = 1;
      int j = 0;
      long l1 = System.currentTimeMillis();
      while (i) {
        i = reapWebhookEventLogs(zonedDateTime);
        j += i;
      } 
      long l2 = System.currentTimeMillis();
      logger.info("Completed reaping [{}] webhook event logs in [{}] ms", Integer.valueOf(j), Long.valueOf(l2 - l1));
      return j;
    } 
    return -1;
  }
  
  private int reapOlderThanDays(int paramInt1, int paramInt2, BiFunction<Integer, ZonedDateTime, ZonedDateTime> paramBiFunction, Function<ZonedDateTime, Integer> paramFunction, String paramString) {
    ZonedDateTime zonedDateTime1 = ZonedDateTime.now(ZoneOffset.UTC).minusDays(paramInt1);
    ZonedDateTime zonedDateTime2 = paramBiFunction.apply(Integer.valueOf(paramInt2), zonedDateTime1);
    if (zonedDateTime2 == null)
      zonedDateTime2 = zonedDateTime1; 
    long l1 = System.currentTimeMillis();
    int i = ((Integer)paramFunction.apply(zonedDateTime2)).intValue();
    long l2 = System.currentTimeMillis();
    logger.debug(paramString + " reaper removed {} rows {} ms", Integer.valueOf(i), Long.valueOf(l2 - l1));
    return i;
  }
  
  private int reapWebhookEventLogs(ZonedDateTime paramZonedDateTime) {
    List<UUID> list = this.backgroundWebhookEventLogMapper.retrieveIdsCreatedBeforeCutoff(paramZonedDateTime, 5000);
    if (!list.isEmpty()) {
      long l1 = System.currentTimeMillis();
      Objects.requireNonNull(this.backgroundWebhookAttemptLogMapper);
      int i = MapperTools.safeDelete(5000, list, this.backgroundWebhookAttemptLogMapper::deleteByWebhookEventLogIds);
      Objects.requireNonNull(this.backgroundWebhookEventLogMapper);
      int j = MapperTools.safeDelete(5000, list, this.backgroundWebhookEventLogMapper::deleteByIds);
      long l2 = System.currentTimeMillis();
      logger.debug("Reaped [{}] attempts and [{}] events in [{}] ms", new Object[] { Integer.valueOf(i), Integer.valueOf(j), Long.valueOf(l2 - l1) });
      return j;
    } 
    return 0;
  }
}
