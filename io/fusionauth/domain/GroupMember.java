package io.fusionauth.domain;

import com.inversoft.json.JacksonConstructor;
import com.inversoft.json.ToString;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class GroupMember implements Buildable<GroupMember> {
  public final Map<String, Object> data = new LinkedHashMap<>();
  
  public UUID groupId;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public UUID userId;
  
  @JacksonConstructor
  public GroupMember() {}
  
  public GroupMember(GroupMember paramGroupMember) {
    this.data.putAll(paramGroupMember.data);
    this.groupId = paramGroupMember.groupId;
    this.id = paramGroupMember.id;
    this.insertInstant = paramGroupMember.insertInstant;
    this.userId = paramGroupMember.userId;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (paramObject == null || getClass() != paramObject.getClass())
      return false; 
    GroupMember groupMember = (GroupMember)paramObject;
    return (Objects.equals(this.data, groupMember.data) && 
      Objects.equals(this.groupId, groupMember.groupId) && 
      Objects.equals(this.id, groupMember.id) && 
      Objects.equals(this.insertInstant, groupMember.insertInstant) && 
      Objects.equals(this.userId, groupMember.userId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.data, this.groupId, this.id, this.insertInstant, this.userId });
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
