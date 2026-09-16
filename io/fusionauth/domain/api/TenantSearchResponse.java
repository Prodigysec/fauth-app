package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Tenant;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

public class TenantSearchResponse {
  public List<Tenant> tenants;
  
  public long total;
  
  @JacksonConstructor
  public TenantSearchResponse() {}
  
  public TenantSearchResponse(SearchResults<Tenant> paramSearchResults) {
    this.tenants = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
