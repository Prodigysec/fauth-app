package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;

public class UserDeactivateEvent extends BaseUserEvent implements Buildable<UserDeactivateEvent> {
  @JacksonConstructor
  public UserDeactivateEvent() {}
  
  public UserDeactivateEvent(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo, paramUser);
  }
  
  public EventType getType() {
    return EventType.UserDeactivate;
  }
}
