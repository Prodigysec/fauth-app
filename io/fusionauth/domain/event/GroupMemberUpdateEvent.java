package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupMemberUpdateEvent extends BaseGroupEvent implements Buildable<GroupMemberUpdateEvent> {
  public List<GroupMember> members = new ArrayList<>();
  
  @JacksonConstructor
  public GroupMemberUpdateEvent() {}
  
  public GroupMemberUpdateEvent(EventInfo paramEventInfo, Group paramGroup, List<GroupMember> paramList) {
    super(paramEventInfo, paramGroup);
    if (paramList != null)
      this.members.addAll(paramList); 
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    GroupMemberUpdateEvent groupMemberUpdateEvent = (GroupMemberUpdateEvent)paramObject;
    return Objects.equals(this.members, groupMemberUpdateEvent.members);
  }
  
  public EventType getType() {
    return EventType.GroupMemberUpdate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.members });
  }
}
