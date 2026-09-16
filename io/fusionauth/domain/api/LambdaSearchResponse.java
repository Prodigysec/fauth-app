package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Lambda;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class LambdaSearchResponse {
  public List<Lambda> lambdas;
  
  public long total;
  
  @JacksonConstructor
  public LambdaSearchResponse() {}
  
  public LambdaSearchResponse(SearchResults<Lambda> paramSearchResults) {
    this.lambdas = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
