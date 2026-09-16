package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.IPAccessControlList;
import java.util.List;

public class IPAccessControlListResponse {
  public IPAccessControlList ipAccessControlList;
  
  public List<IPAccessControlList> ipAccessControlLists;
  
  @JacksonConstructor
  public IPAccessControlListResponse() {}
  
  public IPAccessControlListResponse(IPAccessControlList paramIPAccessControlList) {
    this.ipAccessControlList = paramIPAccessControlList;
  }
  
  public IPAccessControlListResponse(List<IPAccessControlList> paramList) {
    this.ipAccessControlLists = paramList;
  }
}
