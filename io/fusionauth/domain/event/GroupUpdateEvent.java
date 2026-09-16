package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Group;
import java.util.Objects;

public class GroupUpdateEvent extends BaseGroupEvent implements Buildable<GroupUpdateEvent> {
  public Group original;
  
  @JacksonConstructor
  public GroupUpdateEvent() {}
  
  public GroupUpdateEvent(EventInfo paramEventInfo, Group paramGroup1, Group paramGroup2) {
    super(paramEventInfo, paramGroup2);
    this.original = paramGroup1;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    GroupUpdateEvent groupUpdateEvent = (GroupUpdateEvent)paramObject;
    return Objects.equals(this.original, groupUpdateEvent.original);
  }
  
  public EventType getType() {
    return EventType.GroupUpdate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.original });
  }
}
