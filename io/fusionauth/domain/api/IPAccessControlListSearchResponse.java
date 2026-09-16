package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.IPAccessControlList;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class IPAccessControlListSearchResponse {
  public List<IPAccessControlList> ipAccessControlLists;
  
  public long total;
  
  @JacksonConstructor
  public IPAccessControlListSearchResponse() {}
  
  public IPAccessControlListSearchResponse(SearchResults<IPAccessControlList> paramSearchResults) {
    this.ipAccessControlLists = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
