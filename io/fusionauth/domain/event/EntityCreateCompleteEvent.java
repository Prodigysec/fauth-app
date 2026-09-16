package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EventInfo;

public class EntityCreateCompleteEvent extends BaseEntityEvent implements Buildable<EntityCreateCompleteEvent>, NonTransactionalEvent {
  @JacksonConstructor
  public EntityCreateCompleteEvent() {}
  
  public EntityCreateCompleteEvent(EventInfo paramEventInfo, Entity paramEntity) {
    super(paramEventInfo, paramEntity);
  }
  
  public EventType getType() {
    return EventType.EntityCreateComplete;
  }
}
