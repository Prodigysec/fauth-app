package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserLoginFailedReason;
import io.fusionauth.domain.UserLoginFailedReasonCode;
import java.util.Objects;
import java.util.UUID;

public class UserLoginFailedEvent extends BaseUserEvent implements Buildable<UserLoginFailedEvent> {
  public UUID applicationId;
  
  public String authenticationType;
  
  @Deprecated
  public String ipAddress;
  
  public UserLoginFailedReason reason = new UserLoginFailedReason(UserLoginFailedReasonCode.Credentials);
  
  @JacksonConstructor
  public UserLoginFailedEvent() {}
  
  @Deprecated
  public UserLoginFailedEvent(EventInfo paramEventInfo, UUID paramUUID, String paramString, User paramUser) {
    super(paramEventInfo, paramUser);
    this.applicationId = paramUUID;
    this.authenticationType = paramString;
    if (paramEventInfo != null && paramEventInfo.ipAddress != null)
      this.ipAddress = paramEventInfo.ipAddress; 
  }
  
  public UserLoginFailedEvent(EventInfo paramEventInfo, UUID paramUUID, String paramString, UserLoginFailedReason paramUserLoginFailedReason, User paramUser) {
    super(paramEventInfo, paramUser);
    this.applicationId = paramUUID;
    this.authenticationType = paramString;
    if (paramEventInfo != null && paramEventInfo.ipAddress != null)
      this.ipAddress = paramEventInfo.ipAddress; 
    this.reason = paramUserLoginFailedReason;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserLoginFailedEvent userLoginFailedEvent = (UserLoginFailedEvent)paramObject;
    return (Objects.equals(this.applicationId, userLoginFailedEvent.applicationId) && 
      Objects.equals(this.authenticationType, userLoginFailedEvent.authenticationType) && 
      Objects.equals(this.ipAddress, userLoginFailedEvent.ipAddress) && 
      Objects.equals(this.reason, userLoginFailedEvent.reason));
  }
  
  public EventType getType() {
    return EventType.UserLoginFailed;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.authenticationType, this.ipAddress, this.reason });
  }
}
