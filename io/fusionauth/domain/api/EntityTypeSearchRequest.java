package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.EntityTypeSearchCriteria;

public class EntityTypeSearchRequest {
  public EntityTypeSearchCriteria search = new EntityTypeSearchCriteria();
  
  @JacksonConstructor
  public EntityTypeSearchRequest() {}
  
  public EntityTypeSearchRequest(EntityTypeSearchCriteria paramEntityTypeSearchCriteria) {
    this.search = paramEntityTypeSearchCriteria;
  }
}
