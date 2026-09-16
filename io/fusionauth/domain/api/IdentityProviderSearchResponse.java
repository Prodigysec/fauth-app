package io.fusionauth.domain.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.client.json.IdentityProviderSearchResponseDeserializer;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import io.fusionauth.domain.search.SearchResults;
import java.util.List;

@JsonDeserialize(using = IdentityProviderSearchResponseDeserializer.class)
public class IdentityProviderSearchResponse {
  public List<BaseIdentityProvider<?>> identityProviders;
  
  public long total;
  
  @JacksonConstructor
  public IdentityProviderSearchResponse() {}
  
  public IdentityProviderSearchResponse(SearchResults<BaseIdentityProvider<?>> paramSearchResults) {
    this.identityProviders = paramSearchResults.results;
    this.total = paramSearchResults.total;
  }
}
