package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import java.util.Objects;
import java.util.UUID;

public class UserIdentityVerifiedEvent extends BaseUserEvent implements Buildable<UserIdentityVerifiedEvent> {
  public final String loginId;
  
  public final String loginIdType;
  
  public UserIdentityVerifiedEvent(EventInfo paramEventInfo, String paramString1, String paramString2, User paramUser) {
    super(paramEventInfo, paramUser);
    this.loginId = paramString1;
    this.loginIdType = paramString2;
  }
  
  @JacksonConstructor
  private UserIdentityVerifiedEvent() {
    this.loginId = null;
    this.loginIdType = null;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    UserIdentityVerifiedEvent userIdentityVerifiedEvent = (UserIdentityVerifiedEvent)paramObject;
    return (Objects.equals(this.loginId, userIdentityVerifiedEvent.loginId) && Objects.equals(this.loginIdType, userIdentityVerifiedEvent.loginIdType));
  }
  
  public UUID getLinkedObjectId() {
    return (this.user != null) ? super.getLinkedObjectId() : null;
  }
  
  public EventType getType() {
    return EventType.UserIdentityVerified;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.loginId, this.loginIdType });
  }
}
