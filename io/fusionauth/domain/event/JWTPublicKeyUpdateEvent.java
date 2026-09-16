package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class JWTPublicKeyUpdateEvent extends BaseEvent implements Buildable<JWTPublicKeyUpdateEvent> {
  public final Set<UUID> applicationIds;
  
  @JacksonConstructor
  public JWTPublicKeyUpdateEvent() {
    this.applicationIds = new HashSet<>(0);
  }
  
  public JWTPublicKeyUpdateEvent(EventInfo paramEventInfo, UUID paramUUID) {
    super(paramEventInfo);
    this.applicationIds = new HashSet<>();
    this.applicationIds.add(paramUUID);
  }
  
  public JWTPublicKeyUpdateEvent(EventInfo paramEventInfo, Set<UUID> paramSet) {
    super(paramEventInfo);
    this.applicationIds = new HashSet<>(paramSet);
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    JWTPublicKeyUpdateEvent jWTPublicKeyUpdateEvent = (JWTPublicKeyUpdateEvent)paramObject;
    return Objects.equals(this.applicationIds, jWTPublicKeyUpdateEvent.applicationIds);
  }
  
  public EventType getType() {
    return EventType.JWTPublicKeyUpdate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationIds });
  }
}
