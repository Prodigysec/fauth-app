package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.UserComment;
import io.fusionauth.domain.search.SearchResults;
import java.util.ArrayList;
import java.util.List;

public class UserCommentSearchResponse {
  public long total;
  
  public List<UserComment> userComments = new ArrayList<>();
  
  @JacksonConstructor
  public UserCommentSearchResponse() {}
  
  public UserCommentSearchResponse(SearchResults<UserComment> paramSearchResults) {
    this.userComments = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
