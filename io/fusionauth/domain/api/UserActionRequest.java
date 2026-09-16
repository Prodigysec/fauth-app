package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.UserAction;

public class UserActionRequest {
  public UserAction userAction;
  
  @JacksonConstructor
  public UserActionRequest() {}
  
  public UserActionRequest(UserAction paramUserAction) {
    this.userAction = paramUserAction;
  }
}
