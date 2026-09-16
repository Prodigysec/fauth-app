package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EventInfo;

public class EntityDeleteCompleteEvent extends BaseEntityEvent implements Buildable<EntityDeleteCompleteEvent>, NonTransactionalEvent {
  @JacksonConstructor
  public EntityDeleteCompleteEvent() {}
  
  public EntityDeleteCompleteEvent(EventInfo paramEventInfo, Entity paramEntity) {
    super(paramEventInfo, paramEntity);
  }
  
  public EventType getType() {
    return EventType.EntityDeleteComplete;
  }
}
