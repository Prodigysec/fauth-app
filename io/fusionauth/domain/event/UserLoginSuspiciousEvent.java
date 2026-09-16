package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.AuthenticationThreats;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UserLoginSuspiciousEvent extends UserLoginSuccessEvent {
  public Set<AuthenticationThreats> threatsDetected = new LinkedHashSet<>();
  
  @JacksonConstructor
  public UserLoginSuspiciousEvent() {}
  
  public UserLoginSuspiciousEvent(EventInfo paramEventInfo, UUID paramUUID, String paramString, BaseIdentityProvider<?> paramBaseIdentityProvider, User paramUser, Set<AuthenticationThreats> paramSet) {
    super(paramEventInfo, paramUUID, paramString, paramBaseIdentityProvider, paramUser);
    this
      
      .threatsDetected = (Set<AuthenticationThreats>)paramSet.stream().sorted(Comparator.comparing(Enum::name)).collect(Collectors.toCollection(LinkedHashSet::new));
  }
  
  public UserLoginSuspiciousEvent(EventInfo paramEventInfo, UUID paramUUID1, UUID paramUUID2, String paramString, User paramUser, Set<AuthenticationThreats> paramSet) {
    super(paramEventInfo, paramUUID1, paramUUID2, paramString, paramUser);
    this
      
      .threatsDetected = (Set<AuthenticationThreats>)paramSet.stream().sorted(Comparator.comparing(Enum::name)).collect(Collectors.toCollection(LinkedHashSet::new));
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserLoginSuspiciousEvent userLoginSuspiciousEvent = (UserLoginSuspiciousEvent)paramObject;
    return Objects.equals(this.threatsDetected, userLoginSuspiciousEvent.threatsDetected);
  }
  
  public EventType getType() {
    return EventType.UserLoginSuspicious;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.threatsDetected });
  }
}
