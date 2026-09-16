package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.UserActionReason;

public class UserActionReasonRequest {
  public UserActionReason userActionReason;
  
  @JacksonConstructor
  public UserActionReasonRequest() {}
  
  public UserActionReasonRequest(UserActionReason paramUserActionReason) {
    this.userActionReason = paramUserActionReason;
  }
}
