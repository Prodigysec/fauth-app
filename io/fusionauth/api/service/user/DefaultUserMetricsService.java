package io.fusionauth.api.service.user;

import com.google.inject.Inject;
import io.fusionauth.api.domain.IntervalUser;
import io.fusionauth.api.domain.LoginMapper;
import io.fusionauth.api.service.authentication.LoginQueue;
import io.fusionauth.api.service.cache.SystemConfigurationCache;
import io.fusionauth.api.time.TimeUtils;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.SystemConfiguration;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserIdentity;
import io.fusionauth.domain.UserRegistration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.mybatis.guice.transactional.Transactional;

public class DefaultUserMetricsService implements UserMetricsService {
  private final LoginMapper loginMapper;
  
  private final LoginQueue loginQueue;
  
  private final SystemConfigurationCache systemConfigurationCache;
  
  @Inject
  public DefaultUserMetricsService(LoginMapper paramLoginMapper, LoginQueue paramLoginQueue, SystemConfigurationCache paramSystemConfigurationCache) {
    this.loginMapper = paramLoginMapper;
    this.loginQueue = paramLoginQueue;
    this.systemConfigurationCache = paramSystemConfigurationCache;
  }
  
  public void addToLoginQueue(LoginQueue.LoginQueueRawLogin paramLoginQueueRawLogin) {
    this.loginQueue.add(paramLoginQueueRawLogin);
  }
  
  public LoginQueue.LoginQueueRawLogin buildRawLogin(User paramUser, UserIdentity paramUserIdentity, ZonedDateTime paramZonedDateTime, UUID paramUUID, EventInfo paramEventInfo) {
    paramUser.lastLoginInstant = paramZonedDateTime;
    if (paramUserIdentity != null)
      paramUserIdentity.lastLoginInstant = paramZonedDateTime; 
    UserRegistration userRegistration1 = paramUser.getRegistrationForApplication(paramUUID);
    if (userRegistration1 != null)
      userRegistration1.lastLoginInstant = paramZonedDateTime; 
    User user = new User(paramUser);
    UserRegistration userRegistration2 = (userRegistration1 != null) ? new UserRegistration(userRegistration1) : null;
    String str1 = (paramEventInfo != null) ? paramEventInfo.ipAddress : null;
    IdentityType identityType = null;
    String str2 = null;
    if (paramUserIdentity != null) {
      identityType = paramUserIdentity.type;
      str2 = paramUserIdentity.value;
    } 
    return new LoginQueue.LoginQueueRawLogin(user, userRegistration2, paramZonedDateTime, str1, str2, identityType);
  }
  
  @Transactional
  public void updateActiveUserMetrics(UUID paramUUID1, UUID paramUUID2) {
    SystemConfiguration systemConfiguration = this.systemConfigurationCache.get();
    ZoneId zoneId = systemConfiguration.reportTimezone;
    ZonedDateTime zonedDateTime1 = ZonedDateTime.now(ZoneOffset.UTC);
    ZonedDateTime zonedDateTime2 = zonedDateTime1.withZoneSameInstant(zoneId);
    List<IntervalUser> list1 = List.of(new IntervalUser(paramUUID2, TimeUtils.toDay(zonedDateTime2), paramUUID1));
    List<IntervalUser> list2 = List.of(new IntervalUser(paramUUID2, TimeUtils.toMonth(zonedDateTime2), paramUUID1));
    this.loginMapper.upsertRawGlobalDailyActives(list1);
    this.loginMapper.upsertRawGlobalMonthlyActives(list2);
    this.loginMapper.upsertRawApplicationDailyActives(list1);
    this.loginMapper.upsertRawApplicationMonthlyActives(list2);
  }
}
