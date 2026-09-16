package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.util.Objects;
import java.util.UUID;

public class UserRegistrationCreateEvent extends BaseUserEvent implements Buildable<UserRegistrationCreateEvent> {
  public UUID applicationId;
  
  public UserRegistration registration;
  
  public UserRegistrationCreateEvent(EventInfo paramEventInfo, UUID paramUUID, UserRegistration paramUserRegistration, User paramUser) {
    super(paramEventInfo, paramUser);
    this.user.getRegistrations().clear();
    this.applicationId = paramUUID;
    this.registration = paramUserRegistration;
  }
  
  @JacksonConstructor
  private UserRegistrationCreateEvent() {}
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserRegistrationCreateEvent userRegistrationCreateEvent = (UserRegistrationCreateEvent)paramObject;
    return (Objects.equals(this.applicationId, userRegistrationCreateEvent.applicationId) && 
      Objects.equals(this.registration, userRegistrationCreateEvent.registration));
  }
  
  public EventType getType() {
    return EventType.UserRegistrationCreate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.registration });
  }
}
