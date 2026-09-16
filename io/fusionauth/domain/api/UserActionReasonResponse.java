package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.UserActionReason;
import java.util.List;

public class UserActionReasonResponse {
  public UserActionReason userActionReason;
  
  public List<UserActionReason> userActionReasons;
  
  @JacksonConstructor
  public UserActionReasonResponse() {}
  
  public UserActionReasonResponse(UserActionReason paramUserActionReason) {
    this.userActionReason = paramUserActionReason;
  }
  
  public UserActionReasonResponse(List<UserActionReason> paramList) {
    this.userActionReasons = paramList;
  }
}
