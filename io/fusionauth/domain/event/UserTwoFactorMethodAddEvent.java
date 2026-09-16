package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import java.util.Objects;

public class UserTwoFactorMethodAddEvent extends BaseUserEvent implements Buildable<UserTwoFactorMethodAddEvent>, NonTransactionalEvent {
  public TwoFactorMethod method;
  
  @JacksonConstructor
  public UserTwoFactorMethodAddEvent() {}
  
  public UserTwoFactorMethodAddEvent(EventInfo paramEventInfo, TwoFactorMethod paramTwoFactorMethod, User paramUser) {
    super(paramEventInfo, paramUser);
    this.method = paramTwoFactorMethod;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserTwoFactorMethodAddEvent userTwoFactorMethodAddEvent = (UserTwoFactorMethodAddEvent)paramObject;
    return Objects.equals(this.method, userTwoFactorMethodAddEvent.method);
  }
  
  public EventType getType() {
    return EventType.UserTwoFactorMethodAdd;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.method });
  }
}
