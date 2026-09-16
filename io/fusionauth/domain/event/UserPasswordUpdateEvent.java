package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;

public class UserPasswordUpdateEvent extends BaseUserEvent implements Buildable<UserPasswordUpdateEvent>, NonTransactionalEvent {
  @JacksonConstructor
  public UserPasswordUpdateEvent() {}
  
  public UserPasswordUpdateEvent(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo, paramUser);
  }
  
  public EventType getType() {
    return EventType.UserPasswordUpdate;
  }
}
