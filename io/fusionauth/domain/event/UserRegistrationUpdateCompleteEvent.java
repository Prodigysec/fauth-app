package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.util.Objects;
import java.util.UUID;

public class UserRegistrationUpdateCompleteEvent extends BaseUserEvent implements Buildable<UserRegistrationUpdateCompleteEvent>, NonTransactionalEvent {
  public UUID applicationId;
  
  public UserRegistration original;
  
  public UserRegistration registration;
  
  public UserRegistrationUpdateCompleteEvent(EventInfo paramEventInfo, UUID paramUUID, UserRegistration paramUserRegistration1, UserRegistration paramUserRegistration2, User paramUser) {
    super(paramEventInfo, paramUser);
    this.user.getRegistrations().clear();
    this.applicationId = paramUUID;
    this.original = paramUserRegistration1;
    this.registration = paramUserRegistration2;
  }
  
  @JacksonConstructor
  private UserRegistrationUpdateCompleteEvent() {}
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserRegistrationUpdateCompleteEvent userRegistrationUpdateCompleteEvent = (UserRegistrationUpdateCompleteEvent)paramObject;
    return (Objects.equals(this.applicationId, userRegistrationUpdateCompleteEvent.applicationId) && 
      Objects.equals(this.original, userRegistrationUpdateCompleteEvent.original) && 
      Objects.equals(this.registration, userRegistrationUpdateCompleteEvent.registration));
  }
  
  public EventType getType() {
    return EventType.UserRegistrationUpdateComplete;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.original, this.registration });
  }
}
