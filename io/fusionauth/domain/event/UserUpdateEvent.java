package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import java.util.Objects;

public class UserUpdateEvent extends BaseUserEvent implements Buildable<UserUpdateEvent> {
  public final User original;
  
  public UserUpdateEvent(EventInfo paramEventInfo, User paramUser1, User paramUser2) {
    super(paramEventInfo, paramUser2);
    this.original = (new User(paramUser1)).secure().sort();
  }
  
  @JacksonConstructor
  private UserUpdateEvent() {
    this.original = null;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserUpdateEvent userUpdateEvent = (UserUpdateEvent)paramObject;
    return Objects.equals(this.original, userUpdateEvent.original);
  }
  
  public EventType getType() {
    return EventType.UserUpdate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.original });
  }
}
