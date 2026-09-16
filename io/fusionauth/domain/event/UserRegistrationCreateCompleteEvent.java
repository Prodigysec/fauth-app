package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.util.Objects;
import java.util.UUID;

public class UserRegistrationCreateCompleteEvent extends BaseUserEvent implements Buildable<UserRegistrationCreateCompleteEvent>, NonTransactionalEvent {
  public UUID applicationId;
  
  public UserRegistration registration;
  
  public UserRegistrationCreateCompleteEvent(EventInfo paramEventInfo, UUID paramUUID, UserRegistration paramUserRegistration, User paramUser) {
    super(paramEventInfo, paramUser);
    this.user.getRegistrations().clear();
    this.applicationId = paramUUID;
    this.registration = paramUserRegistration;
  }
  
  @JacksonConstructor
  private UserRegistrationCreateCompleteEvent() {}
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserRegistrationCreateCompleteEvent userRegistrationCreateCompleteEvent = (UserRegistrationCreateCompleteEvent)paramObject;
    return (Objects.equals(this.applicationId, userRegistrationCreateCompleteEvent.applicationId) && 
      Objects.equals(this.registration, userRegistrationCreateCompleteEvent.registration));
  }
  
  public EventType getType() {
    return EventType.UserRegistrationCreateComplete;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.registration });
  }
}
