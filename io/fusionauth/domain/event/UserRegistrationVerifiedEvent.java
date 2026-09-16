package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.util.Objects;
import java.util.UUID;

public class UserRegistrationVerifiedEvent extends BaseUserEvent implements Buildable<UserRegistrationVerifiedEvent> {
  public UUID applicationId;
  
  public UserRegistration registration;
  
  public UserRegistrationVerifiedEvent(EventInfo paramEventInfo, UUID paramUUID, UserRegistration paramUserRegistration, User paramUser) {
    super(paramEventInfo, paramUser);
    this.user.getRegistrations().clear();
    this.applicationId = paramUUID;
    this.registration = paramUserRegistration;
  }
  
  @JacksonConstructor
  private UserRegistrationVerifiedEvent() {}
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserRegistrationVerifiedEvent userRegistrationVerifiedEvent = (UserRegistrationVerifiedEvent)paramObject;
    return (Objects.equals(this.applicationId, userRegistrationVerifiedEvent.applicationId) && 
      Objects.equals(this.registration, userRegistrationVerifiedEvent.registration));
  }
  
  public EventType getType() {
    return EventType.UserRegistrationVerified;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.registration });
  }
}
