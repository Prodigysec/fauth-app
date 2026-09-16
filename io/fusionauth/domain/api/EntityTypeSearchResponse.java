package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EntityType;
import io.fusionauth.domain.EntityTypePermission;
import io.fusionauth.domain.search.SearchResults;
import java.util.Comparator;
import java.util.List;

public class EntityTypeSearchResponse {
  public List<EntityType> entityTypes;
  
  public long total;
  
  @JacksonConstructor
  public EntityTypeSearchResponse() {}
  
  public EntityTypeSearchResponse(SearchResults<EntityType> paramSearchResults) {
    this.entityTypes = paramSearchResults.results;
    this.entityTypes.forEach(paramEntityType -> paramEntityType.permissions.sort(Comparator.comparing(())));
    this.total = paramSearchResults.total;
  }
}
