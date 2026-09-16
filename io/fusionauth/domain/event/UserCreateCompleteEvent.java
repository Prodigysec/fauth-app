package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;

public class UserCreateCompleteEvent extends BaseUserEvent implements Buildable<UserCreateCompleteEvent>, NonTransactionalEvent {
  @JacksonConstructor
  public UserCreateCompleteEvent() {}
  
  public UserCreateCompleteEvent(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo, paramUser);
  }
  
  public EventType getType() {
    return EventType.UserCreateComplete;
  }
}
