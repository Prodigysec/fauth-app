package io.fusionauth.domain.event;

import com.inversoft.json.ToString;
import io.fusionauth.domain.EventInfo;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseEvent {
  public ZonedDateTime createInstant;
  
  public UUID id;
  
  public EventInfo info;
  
  public UUID tenantId;
  
  public BaseEvent() {}
  
  public BaseEvent(EventInfo paramEventInfo) {
    this.info = paramEventInfo;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    BaseEvent baseEvent = (BaseEvent)paramObject;
    return (Objects.equals(this.createInstant, baseEvent.createInstant) && 
      Objects.equals(this.id, baseEvent.id) && 
      Objects.equals(this.info, baseEvent.info) && 
      Objects.equals(this.tenantId, baseEvent.tenantId));
  }
  
  public abstract EventType getType();
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.createInstant, this.id, this.info, this.tenantId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
