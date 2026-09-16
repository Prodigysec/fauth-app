package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Group;

public class GroupCreateEvent extends BaseGroupEvent implements Buildable<GroupCreateEvent> {
  @JacksonConstructor
  public GroupCreateEvent() {}
  
  public GroupCreateEvent(EventInfo paramEventInfo, Group paramGroup) {
    super(paramEventInfo, paramGroup);
  }
  
  public EventType getType() {
    return EventType.GroupCreate;
  }
}
