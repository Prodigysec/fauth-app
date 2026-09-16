package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Key;
import io.fusionauth.domain.search.SearchResults;
import java.util.ArrayList;
import java.util.List;

public class KeySearchResponse {
  public List<Key> keys = new ArrayList<>();
  
  public long total;
  
  @JacksonConstructor
  public KeySearchResponse() {}
  
  public KeySearchResponse(SearchResults<Key> paramSearchResults) {
    this.keys = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
