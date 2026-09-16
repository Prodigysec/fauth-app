package io.fusionauth.api.service.authentication;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.fusionauth.api.domain.IntervalCount;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.domain.SystemConfigurationMapper;
import io.fusionauth.api.domain.ZonedDateTimeWrapper;
import io.fusionauth.api.service.system.NodeService;
import io.fusionauth.api.time.TimeUtils;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class LoginAggregator implements Runnable {
  public static final int MINUTES_TO_WAIT_BEFORE_DELETING = 600;
  
  private final LoginMapper backgroundLoginMapper;
  
  private final SystemConfigurationMapper backgroundSystemConfigurationMapper;
  
  private final NodeService nodeService;
  
  @Inject
  public LoginAggregator(@Named("background") LoginMapper paramLoginMapper, @Named("background") SystemConfigurationMapper paramSystemConfigurationMapper, NodeService paramNodeService) {
    this.backgroundLoginMapper = paramLoginMapper;
    this.backgroundSystemConfigurationMapper = paramSystemConfigurationMapper;
    this.nodeService = paramNodeService;
  }
  
  public void run() {
    if (this.nodeService.isMaster()) {
      rollUpDailyActive();
      rollUpMonthlyActive();
    } 
  }
  
  private void rollUpDailyActive() {
    ZoneId zoneId = (this.backgroundSystemConfigurationMapper.retrieve()).reportTimezone;
    ZonedDateTime zonedDateTime = ZonedDateTimeWrapper.now(zoneId);
    int i = TimeUtils.toDay(zonedDateTime);
    List<IntervalCount> list1 = this.backgroundLoginMapper.retrieveRawApplicationDailyActives();
    if (list1.size() > 0) {
      this.backgroundLoginMapper.replaceApplicationDailyActives(list1);
      Objects.requireNonNull(this.backgroundLoginMapper);
      list1.stream().filter(shouldDelete(i, zonedDateTime)).forEach(this.backgroundLoginMapper::deleteRawApplicationDailyActives);
    } 
    List<IntervalCount> list2 = this.backgroundLoginMapper.retrieveRawGlobalDailyActives();
    if (list2.size() > 0) {
      this.backgroundLoginMapper.replaceGlobalDailyActives(list2);
      Objects.requireNonNull(this.backgroundLoginMapper);
      list2.stream().filter(shouldDelete(i, zonedDateTime)).forEach(this.backgroundLoginMapper::deleteRawGlobalDailyActives);
    } 
  }
  
  private void rollUpMonthlyActive() {
    ZoneId zoneId = (this.backgroundSystemConfigurationMapper.retrieve()).reportTimezone;
    ZonedDateTime zonedDateTime = ZonedDateTimeWrapper.now(zoneId);
    int i = TimeUtils.toMonth(zonedDateTime);
    List<IntervalCount> list1 = this.backgroundLoginMapper.retrieveRawApplicationMonthlyActives();
    if (list1.size() > 0) {
      this.backgroundLoginMapper.replaceApplicationMonthlyActives(list1);
      Objects.requireNonNull(this.backgroundLoginMapper);
      list1.stream().filter(shouldDelete(i, zonedDateTime)).forEach(this.backgroundLoginMapper::deleteRawApplicationMonthlyActives);
    } 
    List<IntervalCount> list2 = this.backgroundLoginMapper.retrieveRawGlobalMonthlyActives();
    if (list2.size() > 0) {
      this.backgroundLoginMapper.replaceGlobalMonthlyActives(list2);
      Objects.requireNonNull(this.backgroundLoginMapper);
      list2.stream().filter(shouldDelete(i, zonedDateTime)).forEach(this.backgroundLoginMapper::deleteRawGlobalMonthlyActives);
    } 
  }
  
  private Predicate<IntervalCount> shouldDelete(int paramInt, ZonedDateTime paramZonedDateTime) {
    return paramIntervalCount -> (paramIntervalCount.period < paramInt - 1 || (paramIntervalCount.period < paramInt && paramZonedDateTime.get(ChronoField.MINUTE_OF_DAY) > 600));
  }
}
