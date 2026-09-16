package io.fusionauth.domain.event;

import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Group;
import java.util.Objects;
import java.util.UUID;

public abstract class BaseGroupEvent extends BaseEvent implements ObjectIdentifiable {
  public Group group;
  
  public BaseGroupEvent() {}
  
  public BaseGroupEvent(EventInfo paramEventInfo, Group paramGroup) {
    super(paramEventInfo);
    this.group = paramGroup;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    BaseGroupEvent baseGroupEvent = (BaseGroupEvent)paramObject;
    return Objects.equals(this.group, baseGroupEvent.group);
  }
  
  public UUID getLinkedObjectId() {
    return this.group.id;
  }
  
  public void setLinkedObjectId(UUID paramUUID) {}
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.group });
  }
}
