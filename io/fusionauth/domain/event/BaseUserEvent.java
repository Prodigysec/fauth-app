package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseUserEvent extends BaseEvent implements ObjectIdentifiable {
  public final User user;
  
  @JacksonConstructor
  public BaseUserEvent() {
    this.user = null;
  }
  
  public BaseUserEvent(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo);
    this.user = (paramUser != null) ? (new User(paramUser)).secure().sort() : null;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    BaseUserEvent baseUserEvent = (BaseUserEvent)paramObject;
    return Objects.equals(this.user, baseUserEvent.user);
  }
  
  public UUID getLinkedObjectId() {
    return this.user.id;
  }
  
  public void setLinkedObjectId(UUID paramUUID) {}
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.user });
  }
  
  public static class IdentityInfo {
    public final String type;
    
    public final String value;
    
    @JacksonConstructor
    public IdentityInfo() {
      this.type = null;
      this.value = null;
    }
    
    public IdentityInfo(String param1String1, String param1String2) {
      this.type = param1String1;
      this.value = param1String2;
    }
    
    public boolean equals(Object param1Object) {
      if (this == param1Object)
        return true; 
      if (!(param1Object instanceof IdentityInfo))
        return false; 
      IdentityInfo identityInfo = (IdentityInfo)param1Object;
      return (Objects.equals(this.type, identityInfo.type) && Objects.equals(this.value, identityInfo.value));
    }
    
    public int hashCode() {
      return Objects.hash(new Object[] { this.type, this.value });
    }
  }
}
