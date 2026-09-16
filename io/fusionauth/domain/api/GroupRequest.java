package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.Group;
import java.util.List;
import java.util.UUID;

public class GroupRequest implements Buildable<GroupRequest> {
  public Group group;
  
  public List<UUID> roleIds;
  
  @JacksonConstructor
  public GroupRequest() {}
  
  public GroupRequest(Group paramGroup, List<UUID> paramList) {
    this.group = paramGroup;
    this.roleIds = paramList;
  }
  
  public GroupRequest(Group paramGroup) {
    this.group = paramGroup;
  }
}
