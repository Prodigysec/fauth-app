package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.IdentityProviderSearchCriteria;

public class IdentityProviderSearchRequest {
  public IdentityProviderSearchCriteria search = new IdentityProviderSearchCriteria();
  
  @JacksonConstructor
  public IdentityProviderSearchRequest() {}
  
  public IdentityProviderSearchRequest(IdentityProviderSearchCriteria paramIdentityProviderSearchCriteria) {
    this.search = paramIdentityProviderSearchCriteria;
  }
}
