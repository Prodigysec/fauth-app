package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.EntitySearchCriteria;

public class EntitySearchRequest {
  public EntitySearchCriteria search = new EntitySearchCriteria();
  
  @JacksonConstructor
  public EntitySearchRequest() {}
  
  public EntitySearchRequest(EntitySearchCriteria paramEntitySearchCriteria) {
    this.search = paramEntitySearchCriteria;
  }
}
