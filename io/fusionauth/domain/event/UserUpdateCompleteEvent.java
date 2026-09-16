package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import java.util.Objects;

public class UserUpdateCompleteEvent extends BaseUserEvent implements Buildable<UserUpdateCompleteEvent>, NonTransactionalEvent {
  public final User original;
  
  public UserUpdateCompleteEvent(EventInfo paramEventInfo, User paramUser1, User paramUser2) {
    super(paramEventInfo, paramUser2);
    this.original = (new User(paramUser1)).secure().sort();
  }
  
  @JacksonConstructor
  private UserUpdateCompleteEvent() {
    this.original = null;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserUpdateCompleteEvent userUpdateCompleteEvent = (UserUpdateCompleteEvent)paramObject;
    return Objects.equals(this.original, userUpdateCompleteEvent.original);
  }
  
  public EventType getType() {
    return EventType.UserUpdateComplete;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.original });
  }
}
