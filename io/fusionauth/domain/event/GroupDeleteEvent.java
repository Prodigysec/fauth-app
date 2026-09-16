package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Group;

public class GroupDeleteEvent extends BaseGroupEvent implements Buildable<GroupDeleteEvent> {
  @JacksonConstructor
  public GroupDeleteEvent() {}
  
  public GroupDeleteEvent(EventInfo paramEventInfo, Group paramGroup) {
    super(paramEventInfo, paramGroup);
  }
  
  public EventType getType() {
    return EventType.GroupDelete;
  }
}
