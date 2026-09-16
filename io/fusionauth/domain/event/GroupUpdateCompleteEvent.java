package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Group;
import java.util.Objects;

public class GroupUpdateCompleteEvent extends BaseGroupEvent implements Buildable<GroupUpdateCompleteEvent>, NonTransactionalEvent {
  public Group original;
  
  @JacksonConstructor
  public GroupUpdateCompleteEvent() {}
  
  public GroupUpdateCompleteEvent(EventInfo paramEventInfo, Group paramGroup1, Group paramGroup2) {
    super(paramEventInfo, paramGroup2);
    this.original = paramGroup1;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    GroupUpdateCompleteEvent groupUpdateCompleteEvent = (GroupUpdateCompleteEvent)paramObject;
    return Objects.equals(this.original, groupUpdateCompleteEvent.original);
  }
  
  public EventType getType() {
    return EventType.GroupUpdateComplete;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.original });
  }
}
