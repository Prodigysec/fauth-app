package io.fusionauth.api.service.useraction;

import com.google.inject.Inject;
import io.fusionauth.api.domain.UserActionLogMapper;
import io.fusionauth.api.util.MapperTools;
import io.fusionauth.domain.LogHistory;
import io.fusionauth.domain.UserActionLog;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.mybatis.guice.transactional.Transactional;

public class DefaultUserActionLogService implements UserActionLogService {
  private final UserActionLogMapper userActionLogMapper;
  
  @Inject
  public DefaultUserActionLogService(UserActionLogMapper paramUserActionLogMapper) {
    this.userActionLogMapper = paramUserActionLogMapper;
  }
  
  @Transactional
  public void disassociateActioner(UUID paramUUID) {
    List<UserActionLog> list = this.userActionLogMapper.retrieveAllForActioner(paramUUID);
    if (list != null)
      for (UserActionLog userActionLog : list) {
        if (userActionLog.history == null)
          userActionLog.history = new LogHistory(); 
        userActionLog.history.add(userActionLog.actionerUserId, userActionLog.comment, userActionLog.insertInstant, userActionLog.expiry);
        userActionLog.actionerUserId = null;
        update(userActionLog);
      }  
  }
  
  @Transactional
  public void log(UserActionLog paramUserActionLog) {
    if (paramUserActionLog.id == null)
      paramUserActionLog.id = UUID.randomUUID(); 
    if (paramUserActionLog.insertInstant == null)
      paramUserActionLog.insertInstant = ZonedDateTime.now(ZoneOffset.UTC); 
    this.userActionLogMapper.create(paramUserActionLog);
    MapperTools.safeCreateUpdate(5000, paramUserActionLog.applicationIds, paramList -> this.userActionLogMapper.createApplicationAssociations(paramUserActionLog.id, paramList));
  }
  
  public void markEndEventSent(UserActionLog paramUserActionLog) {
    this.userActionLogMapper.markEndEventSent(paramUserActionLog);
  }
  
  public List<UserActionLog> retrieveAllForUser(UUID paramUUID) {
    return this.userActionLogMapper.retrieveAllForUser(paramUUID);
  }
  
  public UserActionLog retrieveById(UUID paramUUID) {
    return this.userActionLogMapper.retrieveById(paramUUID);
  }
  
  public UserActionLog retrieveCurrent(UUID paramUUID1, UUID paramUUID2) {
    return this.userActionLogMapper.retrieveCurrent(paramUUID1, paramUUID2, ZonedDateTime.now(ZoneOffset.UTC));
  }
  
  public List<UserActionLog> retrieveExpired() {
    ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneOffset.UTC);
    return this.userActionLogMapper.retrieveExpired(zonedDateTime);
  }
  
  @Transactional
  public void update(UserActionLog paramUserActionLog) {
    this.userActionLogMapper.update(paramUserActionLog);
  }
}
