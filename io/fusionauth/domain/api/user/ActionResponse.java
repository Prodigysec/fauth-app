package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.UserActionLog;
import java.util.List;

public class ActionResponse {
  public UserActionLog action;
  
  public List<UserActionLog> actions;
  
  @JacksonConstructor
  public ActionResponse() {}
  
  public ActionResponse(UserActionLog paramUserActionLog) {
    this.action = paramUserActionLog;
  }
  
  public ActionResponse(List<UserActionLog> paramList) {
    this.actions = paramList;
  }
}
