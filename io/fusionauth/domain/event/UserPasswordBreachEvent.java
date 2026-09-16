package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;

public class UserPasswordBreachEvent extends BaseUserEvent implements Buildable<UserPasswordBreachEvent> {
  @JacksonConstructor
  public UserPasswordBreachEvent() {}
  
  public UserPasswordBreachEvent(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo, paramUser);
  }
  
  public EventType getType() {
    return EventType.UserPasswordBreach;
  }
}
