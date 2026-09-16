package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.search.IPAccessControlListSearchCriteria;

public class IPAccessControlListSearchRequest {
  public IPAccessControlListSearchCriteria search = new IPAccessControlListSearchCriteria();
  
  @JacksonConstructor
  public IPAccessControlListSearchRequest() {}
  
  public IPAccessControlListSearchRequest(IPAccessControlListSearchCriteria paramIPAccessControlListSearchCriteria) {
    this.search = paramIPAccessControlListSearchCriteria;
  }
}
