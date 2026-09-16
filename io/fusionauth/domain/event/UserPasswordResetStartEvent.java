package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;

public class UserPasswordResetStartEvent extends BaseUserEvent implements Buildable<UserPasswordResetStartEvent>, NonTransactionalEvent {
  @JacksonConstructor
  public UserPasswordResetStartEvent() {}
  
  public UserPasswordResetStartEvent(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo, paramUser);
  }
  
  public EventType getType() {
    return EventType.UserPasswordResetStart;
  }
}
