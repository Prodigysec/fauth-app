package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.GroupMember;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupMemberAddCompleteEvent extends BaseGroupEvent implements Buildable<GroupMemberAddCompleteEvent>, NonTransactionalEvent {
  public List<GroupMember> members = new ArrayList<>();
  
  @JacksonConstructor
  public GroupMemberAddCompleteEvent() {}
  
  public GroupMemberAddCompleteEvent(EventInfo paramEventInfo, Group paramGroup, List<GroupMember> paramList) {
    super(paramEventInfo, paramGroup);
    if (paramList != null)
      this.members.addAll(paramList); 
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    GroupMemberAddCompleteEvent groupMemberAddCompleteEvent = (GroupMemberAddCompleteEvent)paramObject;
    return Objects.equals(this.members, groupMemberAddCompleteEvent.members);
  }
  
  public EventType getType() {
    return EventType.GroupMemberAddComplete;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.members });
  }
}
