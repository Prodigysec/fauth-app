package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import java.util.Objects;
import java.util.UUID;

public class KickstartSuccessEvent extends BaseEvent implements Buildable<KickstartSuccessEvent>, InstanceEvent, NonTransactionalEvent {
  public UUID instanceId;
  
  @JacksonConstructor
  public KickstartSuccessEvent() {}
  
  public KickstartSuccessEvent(UUID paramUUID) {
    this.instanceId = paramUUID;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    KickstartSuccessEvent kickstartSuccessEvent = (KickstartSuccessEvent)paramObject;
    return Objects.equals(this.instanceId, kickstartSuccessEvent.instanceId);
  }
  
  public EventType getType() {
    return EventType.KickstartSuccess;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.instanceId });
  }
}
