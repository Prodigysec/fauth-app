package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.util.Objects;
import java.util.UUID;

public class UserRegistrationDeleteEvent extends BaseUserEvent implements Buildable<UserRegistrationDeleteEvent> {
  public UUID applicationId;
  
  public UserRegistration registration;
  
  public UserRegistrationDeleteEvent(EventInfo paramEventInfo, UUID paramUUID, UserRegistration paramUserRegistration, User paramUser) {
    super(paramEventInfo, paramUser);
    this.user.getRegistrations().clear();
    this.applicationId = paramUUID;
    this.registration = paramUserRegistration;
  }
  
  @JacksonConstructor
  private UserRegistrationDeleteEvent() {}
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserRegistrationDeleteEvent userRegistrationDeleteEvent = (UserRegistrationDeleteEvent)paramObject;
    return (Objects.equals(this.applicationId, userRegistrationDeleteEvent.applicationId) && 
      Objects.equals(this.registration, userRegistrationDeleteEvent.registration));
  }
  
  public EventType getType() {
    return EventType.UserRegistrationDelete;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.registration });
  }
}
