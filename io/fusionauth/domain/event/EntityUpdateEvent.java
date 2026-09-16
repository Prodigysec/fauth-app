package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EventInfo;
import java.util.Objects;

public class EntityUpdateEvent extends BaseEntityEvent implements Buildable<EntityUpdateEvent> {
  public Entity original;
  
  @JacksonConstructor
  public EntityUpdateEvent() {}
  
  public EntityUpdateEvent(EventInfo paramEventInfo, Entity paramEntity1, Entity paramEntity2) {
    super(paramEventInfo, paramEntity2);
    this.original = (paramEntity1 != null) ? (new Entity(paramEntity1)).secure().sort() : null;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    EntityUpdateEvent entityUpdateEvent = (EntityUpdateEvent)paramObject;
    return Objects.equals(this.original, entityUpdateEvent.original);
  }
  
  public EventType getType() {
    return EventType.EntityUpdate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.original });
  }
}
