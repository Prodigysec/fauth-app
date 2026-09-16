package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.util.Objects;
import java.util.UUID;

public class UserRegistrationUpdateEvent extends BaseUserEvent implements Buildable<UserRegistrationUpdateEvent> {
  public UUID applicationId;
  
  public UserRegistration original;
  
  public UserRegistration registration;
  
  public UserRegistrationUpdateEvent(EventInfo paramEventInfo, UUID paramUUID, UserRegistration paramUserRegistration1, UserRegistration paramUserRegistration2, User paramUser) {
    super(paramEventInfo, paramUser);
    this.user.getRegistrations().clear();
    this.applicationId = paramUUID;
    this.original = paramUserRegistration1;
    this.registration = paramUserRegistration2;
  }
  
  @JacksonConstructor
  private UserRegistrationUpdateEvent() {}
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserRegistrationUpdateEvent userRegistrationUpdateEvent = (UserRegistrationUpdateEvent)paramObject;
    return (Objects.equals(this.applicationId, userRegistrationUpdateEvent.applicationId) && 
      Objects.equals(this.original, userRegistrationUpdateEvent.original) && 
      Objects.equals(this.registration, userRegistrationUpdateEvent.registration));
  }
  
  public EventType getType() {
    return EventType.UserRegistrationUpdate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.original, this.registration });
  }
}
