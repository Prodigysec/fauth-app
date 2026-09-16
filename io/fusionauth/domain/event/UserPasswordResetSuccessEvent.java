package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;

public class UserPasswordResetSuccessEvent extends BaseUserEvent implements Buildable<UserPasswordResetSuccessEvent>, NonTransactionalEvent {
  @JacksonConstructor
  public UserPasswordResetSuccessEvent() {}
  
  public UserPasswordResetSuccessEvent(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo, paramUser);
  }
  
  public EventType getType() {
    return EventType.UserPasswordResetSuccess;
  }
}
