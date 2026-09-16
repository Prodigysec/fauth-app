package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import java.util.List;
import java.util.Objects;

public class UserBulkCreateEvent extends BaseEvent implements Buildable<UserBulkCreateEvent> {
  public List<User> users;
  
  @JacksonConstructor
  public UserBulkCreateEvent() {}
  
  public UserBulkCreateEvent(EventInfo paramEventInfo, List<User> paramList) {
    super(paramEventInfo);
    this.users = paramList;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserBulkCreateEvent userBulkCreateEvent = (UserBulkCreateEvent)paramObject;
    return Objects.equals(this.users, userBulkCreateEvent.users);
  }
  
  public EventType getType() {
    return EventType.UserBulkCreate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.users });
  }
}
