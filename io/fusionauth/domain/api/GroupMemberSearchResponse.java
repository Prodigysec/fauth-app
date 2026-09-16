package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.GroupMember;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class GroupMemberSearchResponse {
  public List<GroupMember> members;
  
  public long total;
  
  @JacksonConstructor
  public GroupMemberSearchResponse() {}
  
  public GroupMemberSearchResponse(SearchResults<GroupMember> paramSearchResults) {
    this.members = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
