package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import java.util.Objects;
import java.util.UUID;

public class UserTwoFactorSuccessEvent extends BaseUserEvent implements Buildable<UserTwoFactorSuccessEvent>, NonTransactionalEvent {
  public UUID applicationId;
  
  public String clientRisk;
  
  public String messageType;
  
  public String method;
  
  @JacksonConstructor
  public UserTwoFactorSuccessEvent() {}
  
  public UserTwoFactorSuccessEvent(EventInfo paramEventInfo, String paramString1, String paramString2, String paramString3, UUID paramUUID, User paramUser) {
    super(paramEventInfo, paramUser);
    this.applicationId = paramUUID;
    this.messageType = paramString2;
    this.method = paramString3;
    this.clientRisk = paramString1;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserTwoFactorSuccessEvent userTwoFactorSuccessEvent = (UserTwoFactorSuccessEvent)paramObject;
    return (Objects.equals(this.applicationId, userTwoFactorSuccessEvent.applicationId) && 
      Objects.equals(this.messageType, userTwoFactorSuccessEvent.messageType) && 
      Objects.equals(this.method, userTwoFactorSuccessEvent.method) && 
      Objects.equals(this.clientRisk, userTwoFactorSuccessEvent.clientRisk));
  }
  
  public EventType getType() {
    return EventType.UserTwoFactorSuccess;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.messageType, this.method, this.clientRisk });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
