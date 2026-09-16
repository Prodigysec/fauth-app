package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Consent;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class ConsentSearchResponse {
  public List<Consent> consents;
  
  public long total;
  
  @JacksonConstructor
  public ConsentSearchResponse() {}
  
  public ConsentSearchResponse(SearchResults<Consent> paramSearchResults) {
    this.consents = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
