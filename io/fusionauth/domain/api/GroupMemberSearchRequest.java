package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.search.GroupMemberSearchCriteria;

public class GroupMemberSearchRequest implements Buildable<GroupMemberSearchRequest> {
  public GroupMemberSearchCriteria search = new GroupMemberSearchCriteria();
  
  @JacksonConstructor
  public GroupMemberSearchRequest() {}
  
  public GroupMemberSearchRequest(GroupMemberSearchCriteria paramGroupMemberSearchCriteria) {
    this.search = paramGroupMemberSearchCriteria;
  }
}
