package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Group;
import java.util.List;

public class GroupResponse {
  public Group group;
  
  public List<Group> groups;
  
  @JacksonConstructor
  public GroupResponse() {}
  
  public GroupResponse(Group paramGroup) {
    this.group = paramGroup;
  }
  
  public GroupResponse(List<Group> paramList) {
    this.groups = paramList;
  }
}
