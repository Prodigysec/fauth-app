package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;

public class UserDeleteEvent extends BaseUserEvent implements Buildable<UserDeleteEvent> {
  @JacksonConstructor
  public UserDeleteEvent() {}
  
  public UserDeleteEvent(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo, paramUser);
  }
  
  public EventType getType() {
    return EventType.UserDelete;
  }
}
