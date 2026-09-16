package io.fusionauth.api.service.authentication;

import com.google.inject.Inject;
import io.fusionauth.api.domain.RequestFrequencyMapper;
import io.fusionauth.api.domain.RequestFrequencyRecord;
import io.fusionauth.api.service.security.RateLimitService;
import io.fusionauth.api.service.useraction.ActionService;
import io.fusionauth.api.service.useraction.UserActionService;
import io.fusionauth.api.util.ActionTools;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.FailedAuthenticationConfiguration;
import io.fusionauth.domain.RateLimitedRequestType;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.api.user.ActionRequest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.mybatis.guice.transactional.Transactional;

public class DefaultFailedLoginService implements FailedLoginService {
  private final ActionService actionService;
  
  private final RateLimitService rateLimitService;
  
  private final RequestFrequencyMapper requestFrequencyMapper;
  
  private final UserActionService userActionService;
  
  @Inject
  public DefaultFailedLoginService(ActionService paramActionService, RateLimitService paramRateLimitService, RequestFrequencyMapper paramRequestFrequencyMapper, UserActionService paramUserActionService) {
    this.actionService = paramActionService;
    this.rateLimitService = paramRateLimitService;
    this.requestFrequencyMapper = paramRequestFrequencyMapper;
    this.userActionService = paramUserActionService;
  }
  
  @Transactional
  public UserActionLog handleFailedLoginCountExceeded(Tenant paramTenant, User paramUser, EventInfo paramEventInfo) {
    if (paramTenant.failedAuthenticationConfiguration.userActionId == null) {
      this.rateLimitService.handleDoNotThrow(paramTenant, RateLimitedRequestType.FailedLogin, paramUser.id.toString());
      return null;
    } 
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    RequestFrequencyRecord requestFrequencyRecord = this.requestFrequencyMapper.retrieveRequestFrequencyRecordForUserAndLock(paramTenant.id, RateLimitedRequestType.FailedLogin, paramUser.id.toString());
    if (requestFrequencyRecord == null) {
      requestFrequencyRecord = new RequestFrequencyRecord(1, paramTenant.id, zonedDateTime, paramUser.id.toString(), RateLimitedRequestType.FailedLogin);
      this.requestFrequencyMapper.createRequestFrequencyRecord(requestFrequencyRecord);
    } else {
      int i = paramTenant.failedAuthenticationConfiguration.resetCountInSeconds;
      if (requestFrequencyRecord.lastUpdateInstant.plusSeconds(i).isBefore(zonedDateTime)) {
        requestFrequencyRecord.count = 1;
      } else {
        requestFrequencyRecord.count++;
      } 
      requestFrequencyRecord.lastUpdateInstant = zonedDateTime;
      this.requestFrequencyMapper.updateRequestFrequencyRecord(requestFrequencyRecord);
    } 
    if (requestFrequencyRecord.count >= paramTenant.failedAuthenticationConfiguration.tooManyAttempts)
      return actionUserForFailedLoginAttempts(paramTenant, paramUser, paramEventInfo); 
    return null;
  }
  
  private UserActionLog actionUserForFailedLoginAttempts(Tenant paramTenant, User paramUser, EventInfo paramEventInfo) {
    FailedAuthenticationConfiguration failedAuthenticationConfiguration = paramTenant.failedAuthenticationConfiguration;
    UserAction userAction = this.userActionService.retrieveById(paramTenant.failedAuthenticationConfiguration.userActionId);
    ActionRequest.ActionData actionData = new ActionRequest.ActionData();
    actionData.actioneeUserId = paramUser.id;
    actionData.comment = "Failed Login Attempts Exceeded.";
    actionData.emailUser = (failedAuthenticationConfiguration.emailUser && userAction.userEmailingEnabled);
    actionData.expiry = ActionTools.getExpirationFrom(failedAuthenticationConfiguration.actionDuration, failedAuthenticationConfiguration.actionDurationUnit);
    actionData.notifyUser = userAction.userNotificationsEnabled;
    actionData.userActionId = userAction.id;
    return this.actionService.actionUser(paramTenant, paramTenant, paramUser, userAction, null, actionData, true, paramEventInfo);
  }
}
