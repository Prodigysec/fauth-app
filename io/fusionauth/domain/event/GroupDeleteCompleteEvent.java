package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Group;

public class GroupDeleteCompleteEvent extends BaseGroupEvent implements Buildable<GroupDeleteCompleteEvent>, NonTransactionalEvent {
  @JacksonConstructor
  public GroupDeleteCompleteEvent() {}
  
  public GroupDeleteCompleteEvent(EventInfo paramEventInfo, Group paramGroup) {
    super(paramEventInfo, paramGroup);
  }
  
  public EventType getType() {
    return EventType.GroupDeleteComplete;
  }
}
