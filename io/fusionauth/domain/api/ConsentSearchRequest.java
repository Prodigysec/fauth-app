package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.ConsentSearchCriteria;

public class ConsentSearchRequest {
  public ConsentSearchCriteria search = new ConsentSearchCriteria();
  
  @JacksonConstructor
  public ConsentSearchRequest() {}
  
  public ConsentSearchRequest(ConsentSearchCriteria paramConsentSearchCriteria) {
    this.search = paramConsentSearchCriteria;
  }
}
