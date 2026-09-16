package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.UserAction;
import java.util.List;

public class UserActionResponse {
  public UserAction userAction;
  
  public List<UserAction> userActions;
  
  @JacksonConstructor
  public UserActionResponse() {}
  
  public UserActionResponse(UserAction paramUserAction) {
    this.userAction = paramUserAction;
  }
  
  public UserActionResponse(List<UserAction> paramList) {
    this.userActions = paramList;
  }
}
