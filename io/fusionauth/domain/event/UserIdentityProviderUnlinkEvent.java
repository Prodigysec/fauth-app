package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.User;
import java.util.Objects;

public class UserIdentityProviderUnlinkEvent extends BaseUserEvent implements Buildable<UserIdentityProviderUnlinkEvent>, NonTransactionalEvent {
  public IdentityProviderLink identityProviderLink;
  
  @JacksonConstructor
  public UserIdentityProviderUnlinkEvent() {}
  
  public UserIdentityProviderUnlinkEvent(EventInfo paramEventInfo, IdentityProviderLink paramIdentityProviderLink, User paramUser) {
    super(paramEventInfo, paramUser);
    this.identityProviderLink = paramIdentityProviderLink;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserIdentityProviderUnlinkEvent userIdentityProviderUnlinkEvent = (UserIdentityProviderUnlinkEvent)paramObject;
    return Objects.equals(this.identityProviderLink, userIdentityProviderUnlinkEvent.identityProviderLink);
  }
  
  public EventType getType() {
    return EventType.UserIdentityProviderUnlink;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.identityProviderLink });
  }
}
