package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.search.GroupSearchCriteria;

public class GroupSearchRequest implements Buildable<GroupSearchRequest> {
  public GroupSearchCriteria search = new GroupSearchCriteria();
  
  @JacksonConstructor
  public GroupSearchRequest() {}
  
  public GroupSearchRequest(GroupSearchCriteria paramGroupSearchCriteria) {
    this.search = paramGroupSearchCriteria;
  }
}
