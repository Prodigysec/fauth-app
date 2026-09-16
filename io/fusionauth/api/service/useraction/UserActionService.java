package io.fusionauth.api.service.useraction;

import com.inversoft.error.Errors;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserActionLog;
import java.util.List;
import java.util.UUID;

public interface UserActionService {
  void create(UserAction paramUserAction);
  
  boolean deactivate(UUID paramUUID);
  
  boolean delete(UUID paramUUID);
  
  UserAction reactivate(UUID paramUUID);
  
  List<UserAction> retrieveAll();
  
  List<UserAction> retrieveAllCurrentForUser(UUID paramUUID);
  
  List<UserActionLog> retrieveAllCurrentPreventLoginActionLogsForUser(UUID paramUUID);
  
  List<UserAction> retrieveAllInactive();
  
  UserAction retrieveById(UUID paramUUID);
  
  UserAction retrieveByIdIgnoreActive(UUID paramUUID);
  
  UserAction retrieveByName(String paramString);
  
  UserAction retrieveByNameIgnoreInactive(String paramString);
  
  boolean update(UserAction paramUserAction);
  
  Errors validate(UserAction paramUserAction, boolean paramBoolean);
}
