package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.KeySearchCriteria;

public class KeySearchRequest {
  public KeySearchCriteria search = new KeySearchCriteria();
  
  @JacksonConstructor
  public KeySearchRequest() {}
  
  public KeySearchRequest(KeySearchCriteria paramKeySearchCriteria) {
    this.search = paramKeySearchCriteria;
  }
}
