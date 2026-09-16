package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.IdentityProviderLink;
import io.fusionauth.domain.User;
import java.util.Objects;

public class UserIdentityProviderLinkEvent extends BaseUserEvent implements Buildable<UserIdentityProviderLinkEvent>, NonTransactionalEvent {
  public IdentityProviderLink identityProviderLink;
  
  @JacksonConstructor
  public UserIdentityProviderLinkEvent() {}
  
  public UserIdentityProviderLinkEvent(EventInfo paramEventInfo, IdentityProviderLink paramIdentityProviderLink, User paramUser) {
    super(paramEventInfo, paramUser);
    this.identityProviderLink = paramIdentityProviderLink;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserIdentityProviderLinkEvent userIdentityProviderLinkEvent = (UserIdentityProviderLinkEvent)paramObject;
    return Objects.equals(this.identityProviderLink, userIdentityProviderLinkEvent.identityProviderLink);
  }
  
  public EventType getType() {
    return EventType.UserIdentityProviderLink;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.identityProviderLink });
  }
}
