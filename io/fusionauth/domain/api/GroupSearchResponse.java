package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Group;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class GroupSearchResponse {
  public List<Group> groups;
  
  public long total;
  
  @JacksonConstructor
  public GroupSearchResponse() {}
  
  public GroupSearchResponse(SearchResults<Group> paramSearchResults) {
    this.groups = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
