package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;

public class ReindexRequest {
  public String index;
  
  @JacksonConstructor
  public ReindexRequest() {}
  
  public ReindexRequest(String paramString) {
    this.index = paramString;
  }
}
