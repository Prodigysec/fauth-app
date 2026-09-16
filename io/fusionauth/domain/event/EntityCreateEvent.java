package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EventInfo;

public class EntityCreateEvent extends BaseEntityEvent implements Buildable<EntityCreateEvent> {
  @JacksonConstructor
  public EntityCreateEvent() {}
  
  public EntityCreateEvent(EventInfo paramEventInfo, Entity paramEntity) {
    super(paramEventInfo, paramEntity);
  }
  
  public EventType getType() {
    return EventType.EntityCreate;
  }
}
