package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EntityGrant;
import io.fusionauth.domain.EntityTypePermission;
import io.fusionauth.domain.search.SearchResults;
import java.util.Comparator;
import java.util.List;

public class EntityGrantSearchResponse {
  public List<EntityGrant> grants;
  
  public long total;
  
  @JacksonConstructor
  public EntityGrantSearchResponse() {}
  
  public EntityGrantSearchResponse(SearchResults<EntityGrant> paramSearchResults) {
    this.grants = paramSearchResults.results;
    this.total = paramSearchResults.total;
    this.grants.forEach(paramEntityGrant -> paramEntityGrant.entity.type.permissions.sort(Comparator.comparing(())));
  }
}
