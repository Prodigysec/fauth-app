package io.fusionauth.api.service.useraction;

import io.fusionauth.api.service.BaseValidationResult;
import io.fusionauth.domain.UserActionReason;
import java.util.List;
import java.util.UUID;

public interface UserActionReasonService {
  void create(UserActionReason paramUserActionReason);
  
  boolean delete(UUID paramUUID);
  
  List<UserActionReason> retrieveAll();
  
  UserActionReason retrieveByCode(String paramString);
  
  UserActionReason retrieveById(UUID paramUUID);
  
  UserActionReason retrieveByText(String paramString);
  
  void update(UserActionReason paramUserActionReason1, UserActionReason paramUserActionReason2);
  
  ValidationResult validate(UserActionReason paramUserActionReason, boolean paramBoolean);
  
  public static class ValidationResult extends BaseValidationResult {
    public UserActionReason existing;
    
    public UserActionReason reason;
  }
}
