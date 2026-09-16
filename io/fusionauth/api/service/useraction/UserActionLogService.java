package io.fusionauth.api.service.useraction;

import io.fusionauth.domain.UserActionLog;
import java.util.List;
import java.util.UUID;

public interface UserActionLogService {
  void disassociateActioner(UUID paramUUID);
  
  void log(UserActionLog paramUserActionLog);
  
  void markEndEventSent(UserActionLog paramUserActionLog);
  
  List<UserActionLog> retrieveAllForUser(UUID paramUUID);
  
  UserActionLog retrieveById(UUID paramUUID);
  
  UserActionLog retrieveCurrent(UUID paramUUID1, UUID paramUUID2);
  
  List<UserActionLog> retrieveExpired();
  
  void update(UserActionLog paramUserActionLog);
}
