package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import java.util.Objects;
import java.util.UUID;

public class UserTwoFactorChallengeEvent extends BaseUserEvent implements Buildable<UserTwoFactorChallengeEvent>, NonTransactionalEvent {
  public UUID applicationId;
  
  public String clientRisk;
  
  @JacksonConstructor
  public UserTwoFactorChallengeEvent() {}
  
  public UserTwoFactorChallengeEvent(EventInfo paramEventInfo, String paramString, UUID paramUUID, User paramUser) {
    super(paramEventInfo, paramUser);
    this.applicationId = paramUUID;
    this.clientRisk = paramString;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserTwoFactorChallengeEvent userTwoFactorChallengeEvent = (UserTwoFactorChallengeEvent)paramObject;
    return (Objects.equals(this.applicationId, userTwoFactorChallengeEvent.applicationId) && 
      Objects.equals(this.clientRisk, userTwoFactorChallengeEvent.clientRisk));
  }
  
  public EventType getType() {
    return EventType.UserTwoFactorChallenge;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.clientRisk });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
