package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class ApplicationSearchResponse extends ExpandableResponse {
  public List<Application> applications;
  
  public long total;
  
  @JacksonConstructor
  public ApplicationSearchResponse() {}
  
  public ApplicationSearchResponse(SearchResults<Application> paramSearchResults) {
    this.applications = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
