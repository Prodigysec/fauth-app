package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.util.Objects;
import java.util.UUID;

public class UserRegistrationDeleteCompleteEvent extends BaseUserEvent implements Buildable<UserRegistrationDeleteCompleteEvent>, NonTransactionalEvent {
  public UUID applicationId;
  
  public UserRegistration registration;
  
  public UserRegistrationDeleteCompleteEvent(EventInfo paramEventInfo, UUID paramUUID, UserRegistration paramUserRegistration, User paramUser) {
    super(paramEventInfo, paramUser);
    this.user.getRegistrations().clear();
    this.applicationId = paramUUID;
    this.registration = paramUserRegistration;
  }
  
  @JacksonConstructor
  private UserRegistrationDeleteCompleteEvent() {}
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserRegistrationDeleteCompleteEvent userRegistrationDeleteCompleteEvent = (UserRegistrationDeleteCompleteEvent)paramObject;
    return (Objects.equals(this.applicationId, userRegistrationDeleteCompleteEvent.applicationId) && 
      Objects.equals(this.registration, userRegistrationDeleteCompleteEvent.registration));
  }
  
  public EventType getType() {
    return EventType.UserRegistrationDeleteComplete;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.registration });
  }
}
