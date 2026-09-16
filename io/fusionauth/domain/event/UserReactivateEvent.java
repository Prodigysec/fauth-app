package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;

public class UserReactivateEvent extends BaseUserEvent implements Buildable<UserReactivateEvent> {
  @JacksonConstructor
  public UserReactivateEvent() {}
  
  public UserReactivateEvent(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo, paramUser);
  }
  
  public EventType getType() {
    return EventType.UserReactivate;
  }
}
