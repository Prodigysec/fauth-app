package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;

public class UserEmailVerifiedEvent extends BaseUserEvent implements Buildable<UserEmailVerifiedEvent> {
  @JacksonConstructor
  public UserEmailVerifiedEvent() {}
  
  public UserEmailVerifiedEvent(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo, paramUser);
  }
  
  public EventType getType() {
    return EventType.UserEmailVerified;
  }
}
