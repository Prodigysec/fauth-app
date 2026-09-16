package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.EntityGrantSearchCriteria;

public class EntityGrantSearchRequest {
  public EntityGrantSearchCriteria search = new EntityGrantSearchCriteria();
  
  @JacksonConstructor
  public EntityGrantSearchRequest() {}
  
  public EntityGrantSearchRequest(EntityGrantSearchCriteria paramEntityGrantSearchCriteria) {
    this.search = paramEntityGrantSearchCriteria;
  }
}
