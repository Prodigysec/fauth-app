package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import java.util.Objects;
import java.util.UUID;

public class UserTwoFactorFailedAttemptEvent extends BaseUserEvent implements Buildable<UserTwoFactorFailedAttemptEvent>, NonTransactionalEvent {
  public UUID applicationId;
  
  public String clientRisk;
  
  public String messageType;
  
  public String method;
  
  @JacksonConstructor
  public UserTwoFactorFailedAttemptEvent() {}
  
  public UserTwoFactorFailedAttemptEvent(EventInfo paramEventInfo, String paramString1, String paramString2, String paramString3, UUID paramUUID, User paramUser) {
    super(paramEventInfo, paramUser);
    this.clientRisk = paramString1;
    this.applicationId = paramUUID;
    this.method = paramString3;
    this.messageType = paramString2;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserTwoFactorFailedAttemptEvent userTwoFactorFailedAttemptEvent = (UserTwoFactorFailedAttemptEvent)paramObject;
    return (Objects.equals(this.applicationId, userTwoFactorFailedAttemptEvent.applicationId) && 
      Objects.equals(this.method, userTwoFactorFailedAttemptEvent.method) && 
      Objects.equals(this.messageType, userTwoFactorFailedAttemptEvent.messageType) && 
      Objects.equals(this.clientRisk, userTwoFactorFailedAttemptEvent.clientRisk));
  }
  
  public EventType getType() {
    return EventType.UserTwoFactorFailedAttempt;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.messageType, this.method, this.clientRisk });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
