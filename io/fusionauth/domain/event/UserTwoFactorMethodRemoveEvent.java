package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import java.util.Objects;

public class UserTwoFactorMethodRemoveEvent extends BaseUserEvent implements Buildable<UserTwoFactorMethodRemoveEvent>, NonTransactionalEvent {
  public TwoFactorMethod method;
  
  @JacksonConstructor
  public UserTwoFactorMethodRemoveEvent() {}
  
  public UserTwoFactorMethodRemoveEvent(EventInfo paramEventInfo, TwoFactorMethod paramTwoFactorMethod, User paramUser) {
    super(paramEventInfo, paramUser);
    this.method = paramTwoFactorMethod;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserTwoFactorMethodRemoveEvent userTwoFactorMethodRemoveEvent = (UserTwoFactorMethodRemoveEvent)paramObject;
    return Objects.equals(this.method, userTwoFactorMethodRemoveEvent.method);
  }
  
  public EventType getType() {
    return EventType.UserTwoFactorMethodRemove;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.method });
  }
}
