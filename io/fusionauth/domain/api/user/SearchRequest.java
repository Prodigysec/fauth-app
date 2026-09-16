package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.api.ExpandableRequest;
import io.fusionauth.domain.search.UserSearchCriteria;

public class SearchRequest extends ExpandableRequest implements Buildable<SearchRequest> {
  public UserSearchCriteria search;
  
  @JacksonConstructor
  public SearchRequest() {}
  
  public SearchRequest(UserSearchCriteria paramUserSearchCriteria) {
    this.search = paramUserSearchCriteria;
  }
}
