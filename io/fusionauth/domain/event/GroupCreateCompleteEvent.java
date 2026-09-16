package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Group;

public class GroupCreateCompleteEvent extends BaseGroupEvent implements Buildable<GroupCreateCompleteEvent>, NonTransactionalEvent {
  @JacksonConstructor
  public GroupCreateCompleteEvent() {}
  
  public GroupCreateCompleteEvent(EventInfo paramEventInfo, Group paramGroup) {
    super(paramEventInfo, paramGroup);
  }
  
  public EventType getType() {
    return EventType.GroupCreateComplete;
  }
}
