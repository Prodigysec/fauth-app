package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;

public class UserPasswordResetSendEvent extends BaseUserEvent implements Buildable<UserPasswordResetSendEvent>, NonTransactionalEvent {
  @JacksonConstructor
  public UserPasswordResetSendEvent() {}
  
  public UserPasswordResetSendEvent(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo, paramUser);
  }
  
  public EventType getType() {
    return EventType.UserPasswordResetSend;
  }
}
