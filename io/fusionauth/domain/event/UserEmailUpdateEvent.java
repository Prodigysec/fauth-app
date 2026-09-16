package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import java.util.Objects;

public class UserEmailUpdateEvent extends BaseUserEvent implements Buildable<UserEmailUpdateEvent>, NonTransactionalEvent {
  public String previousEmail;
  
  public UserEmailUpdateEvent(EventInfo paramEventInfo, String paramString, User paramUser) {
    super(paramEventInfo, paramUser);
    this.previousEmail = paramString;
  }
  
  @JacksonConstructor
  public UserEmailUpdateEvent() {}
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserEmailUpdateEvent userEmailUpdateEvent = (UserEmailUpdateEvent)paramObject;
    return Objects.equals(this.previousEmail, userEmailUpdateEvent.previousEmail);
  }
  
  public EventType getType() {
    return EventType.UserEmailUpdate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.previousEmail });
  }
}
