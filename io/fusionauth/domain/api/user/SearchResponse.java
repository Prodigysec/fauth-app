package io.fusionauth.domain.api.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.User;
import io.fusionauth.domain.api.ExpandableResponse;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class SearchResponse extends ExpandableResponse {
  public long total;
  
  public String nextResults;
  
  @JsonIgnore
  public boolean totalEqualToActual;
  
  public List<User> users;
  
  @JacksonConstructor
  public SearchResponse() {}
  
  public SearchResponse(SearchResults<User> paramSearchResults) {
    this.total = paramSearchResults.total;
    this.users = paramSearchResults.results;
  }
}
