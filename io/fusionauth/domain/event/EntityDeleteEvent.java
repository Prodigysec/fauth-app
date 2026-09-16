package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EventInfo;

public class EntityDeleteEvent extends BaseEntityEvent implements Buildable<EntityDeleteEvent> {
  @JacksonConstructor
  public EntityDeleteEvent() {}
  
  public EntityDeleteEvent(EventInfo paramEventInfo, Entity paramEntity) {
    super(paramEventInfo, paramEntity);
  }
  
  public EventType getType() {
    return EventType.EntityDelete;
  }
}
