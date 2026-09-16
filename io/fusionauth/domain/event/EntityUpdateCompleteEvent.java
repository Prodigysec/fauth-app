package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EventInfo;
import java.util.Objects;

public class EntityUpdateCompleteEvent extends BaseEntityEvent implements Buildable<EntityUpdateCompleteEvent>, NonTransactionalEvent {
  public Entity original;
  
  @JacksonConstructor
  public EntityUpdateCompleteEvent() {}
  
  public EntityUpdateCompleteEvent(EventInfo paramEventInfo, Entity paramEntity1, Entity paramEntity2) {
    super(paramEventInfo, paramEntity2);
    this.original = (paramEntity1 != null) ? (new Entity(paramEntity1)).secure().sort() : null;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    EntityUpdateCompleteEvent entityUpdateCompleteEvent = (EntityUpdateCompleteEvent)paramObject;
    return Objects.equals(this.original, entityUpdateCompleteEvent.original);
  }
  
  public EventType getType() {
    return EventType.EntityUpdateComplete;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.original });
  }
}
