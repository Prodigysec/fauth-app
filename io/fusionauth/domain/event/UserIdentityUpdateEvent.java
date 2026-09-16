package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import java.util.Objects;
import java.util.UUID;

public class UserIdentityUpdateEvent extends BaseUserEvent implements Buildable<UserIdentityUpdateEvent> {
  public final String loginIdType;
  
  public final String newLoginId;
  
  public final String previousLoginId;
  
  public UserIdentityUpdateEvent(EventInfo paramEventInfo, String paramString1, String paramString2, String paramString3, User paramUser) {
    super(paramEventInfo, paramUser);
    this.previousLoginId = paramString1;
    this.newLoginId = paramString2;
    this.loginIdType = paramString3;
  }
  
  @JacksonConstructor
  private UserIdentityUpdateEvent() {
    this.previousLoginId = null;
    this.newLoginId = null;
    this.loginIdType = null;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    if (!super.equals(paramObject))
      return false; 
    UserIdentityUpdateEvent userIdentityUpdateEvent = (UserIdentityUpdateEvent)paramObject;
    return (
      Objects.equals(this.previousLoginId, userIdentityUpdateEvent.previousLoginId) && 
      Objects.equals(this.newLoginId, userIdentityUpdateEvent.newLoginId) && 
      Objects.equals(this.loginIdType, userIdentityUpdateEvent.loginIdType));
  }
  
  public UUID getLinkedObjectId() {
    return (this.user != null) ? super.getLinkedObjectId() : null;
  }
  
  public EventType getType() {
    return EventType.UserIdentityUpdate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.previousLoginId, this.newLoginId, this.loginIdType });
  }
}
