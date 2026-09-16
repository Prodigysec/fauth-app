package io.fusionauth.api.service.useraction;

import com.inversoft.error.Errors;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserAction;
import io.fusionauth.domain.UserActionLog;
import io.fusionauth.domain.UserActionReason;
import io.fusionauth.domain.api.user.ActionRequest;
import io.fusionauth.domain.event.UserActionPhase;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface ActionService {
  UserActionLog actionUser(Tenant paramTenant1, Tenant paramTenant2, User paramUser, UserAction paramUserAction, UserActionReason paramUserActionReason, ActionRequest.ActionData paramActionData, boolean paramBoolean, EventInfo paramEventInfo) throws UserAlreadyActionedException;
  
  UserActionLog cancelAction(Tenant paramTenant, User paramUser, UserAction paramUserAction, UserActionLog paramUserActionLog, ActionRequest.ActionData paramActionData, boolean paramBoolean, EventInfo paramEventInfo);
  
  void handleFromCleanSpeak(UUID paramUUID, CleanSpeakUserAction paramCleanSpeakUserAction);
  
  List<UserActionLog> retrieveAllForUser(User paramUser, Boolean paramBoolean);
  
  List<UserActionLog> retrieveAllForUserPreventingLogin(User paramUser);
  
  UserActionLog retrieveById(UUID paramUUID);
  
  void sendEndEvents(EventInfo paramEventInfo);
  
  UserActionLog updateAction(Tenant paramTenant, User paramUser, UserAction paramUserAction, UserActionLog paramUserActionLog, ActionRequest.ActionData paramActionData, boolean paramBoolean, EventInfo paramEventInfo) throws UserAlreadyActionedException;
  
  ValidationResult validate(UUID paramUUID, ActionRequest.ActionData paramActionData);
  
  ValidationResult validateRetrieve(UUID paramUUID1, UUID paramUUID2);
  
  ValidationResult validateUpdateOrEnd(UUID paramUUID1, UUID paramUUID2, ActionRequest.ActionData paramActionData, boolean paramBoolean);
  
  public static class CleanSpeakUserAction {
    public String action;
    
    public List<UUID> applicationIds;
    
    public String comment;
    
    public ZonedDateTime expiry;
    
    public String key;
    
    public String localizedAction;
    
    public String localizedDuration;
    
    public String localizedKey;
    
    public String localizedReason;
    
    public UUID moderatorId;
    
    public boolean notifyUser;
    
    public UserActionPhase phase;
    
    public String reason;
    
    public String reasonCode;
    
    public UUID userId;
    
    public CleanSpeakUserAction(String param1String1, List<UUID> param1List, String param1String2, ZonedDateTime param1ZonedDateTime, String param1String3, String param1String4, String param1String5, String param1String6, String param1String7, UUID param1UUID1, boolean param1Boolean, UserActionPhase param1UserActionPhase, String param1String8, String param1String9, UUID param1UUID2) {
      this.action = param1String1;
      this.applicationIds = param1List;
      this.comment = param1String2;
      this.expiry = param1ZonedDateTime;
      this.key = param1String3;
      this.localizedAction = param1String4;
      this.localizedDuration = param1String5;
      this.localizedKey = param1String6;
      this.localizedReason = param1String7;
      this.moderatorId = param1UUID1;
      this.notifyUser = param1Boolean;
      this.phase = param1UserActionPhase;
      this.reason = param1String8;
      this.reasonCode = param1String9;
      this.userId = param1UUID2;
    }
  }
  
  public static class ValidationResult {
    public UserAction action;
    
    public Tenant actioneeTenant;
    
    public User actioneeUser;
    
    public Tenant actionerTenant;
    
    public User actionerUser;
    
    public Errors errors;
    
    public UserActionLog log;
    
    public UserActionReason reason;
  }
}
