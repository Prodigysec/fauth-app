package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.search.TenantSearchCriteria;

public class TenantSearchRequest implements Buildable<TenantSearchRequest> {
  public TenantSearchCriteria search = new TenantSearchCriteria();
  
  @JacksonConstructor
  public TenantSearchRequest() {}
  
  public TenantSearchRequest(TenantSearchCriteria paramTenantSearchCriteria) {
    this.search = paramTenantSearchCriteria;
  }
}
