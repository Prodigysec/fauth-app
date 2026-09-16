package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;

public class UserCreateEvent extends BaseUserEvent implements Buildable<UserCreateEvent> {
  @JacksonConstructor
  public UserCreateEvent() {}
  
  public UserCreateEvent(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo, paramUser);
  }
  
  public EventType getType() {
    return EventType.UserCreate;
  }
}
