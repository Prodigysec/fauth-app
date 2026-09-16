package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.IPAccessControlList;

public class IPAccessControlListRequest {
  public IPAccessControlList ipAccessControlList;
  
  @JacksonConstructor
  public IPAccessControlListRequest() {}
  
  public IPAccessControlListRequest(IPAccessControlList paramIPAccessControlList) {
    this.ipAccessControlList = paramIPAccessControlList;
  }
}
