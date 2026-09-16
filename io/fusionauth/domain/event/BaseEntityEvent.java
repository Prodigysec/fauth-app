package io.fusionauth.domain.event;

import io.fusionauth.domain.Entity;
import io.fusionauth.domain.EventInfo;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseEntityEvent extends BaseEvent implements ObjectIdentifiable {
  public Entity entity;
  
  public BaseEntityEvent() {}
  
  public BaseEntityEvent(EventInfo paramEventInfo, Entity paramEntity) {
    super(paramEventInfo);
    this.entity = (paramEntity != null) ? (new Entity(paramEntity)).secure().sort() : null;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    BaseEntityEvent baseEntityEvent = (BaseEntityEvent)paramObject;
    return Objects.equals(this.entity, baseEntityEvent.entity);
  }
  
  public UUID getLinkedObjectId() {
    return this.entity.id;
  }
  
  public void setLinkedObjectId(UUID paramUUID) {}
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.entity });
  }
}
